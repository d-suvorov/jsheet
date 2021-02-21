package org.jsheet.model;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import org.jsheet.parser.ParserUtils;

import javax.swing.table.AbstractTableModel;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JSheetTableModel extends AbstractTableModel {
    private static final int DEFAULT_ROW_COUNT = 50;
    private static final int DEFAULT_COLUMN_COUNT = 26;

    private final List<Value[]> data;
    private final DependencyManager dependencies = new DependencyManager();
    private boolean modified = false;

    public JSheetTableModel() {
        this(DEFAULT_ROW_COUNT, DEFAULT_COLUMN_COUNT);
    }

    public JSheetTableModel(int rowCount, int columnCount) {
        if (rowCount == 0 || columnCount == 0)
            throw new IllegalArgumentException();
        data = new ArrayList<>(rowCount);
        for (int i = 0; i < rowCount; i++) {
            data.add(new Value[columnCount]);
        }
    }

    /**
     * Takes a raw {@code data} of strings and tries to reinsert them as model values.
     */
    private JSheetTableModel(List<Value[]> data) {
        this.data = data;
        for (int row = 0; row < getRowCount(); row++) {
            for (int column = 0; column < getColumnCount(); column++) {
                Object value = getValueAt(row, column);
                // tries to parse functions and numbers from strings
                setValueAt(value, row, column);
            }
        }
    }

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public int getColumnCount() {
        return data.get(0).length;
    }

    @Override
    public Value getValueAt(int rowIndex, int columnIndex) {
        return data.get(rowIndex)[columnIndex];
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return true;
    }

    /**
     * 1. If a current value is a formula, removes links to cells {@code referencedBy} it.
     * 2. If a new value is a formula, adds links to cells {@code referencedBy} it.
     * 3. Invalidate formulae results current cell is transitively {@code referencedBy}.
     **/
    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        setModified(true);
        JSheetCell current = new JSheetCell(rowIndex, columnIndex);
        Value prev = getValueAt(rowIndex, columnIndex);
        if (prev != null && prev.getTag() == Value.Type.EXPR) {
            ExprWrapper wrapper = prev.getAsExpr();
            dependencies.removeFormula(current, wrapper);
        }
        data.get(rowIndex)[columnIndex] = getModelValue(value, current);
        dependencies.recomputeAll(current);
    }

    private Value getModelValue(Object value, JSheetCell current) {
        if (value == null)
            return null;
        if (value instanceof String) {
            String strValue = (String) value;
            if (strValue.startsWith("=")) {
                ExprWrapper wrapper = ParserUtils.parse(strValue);
                wrapper.resolveRefs(this);
                dependencies.addFormula(current, wrapper);
                return Value.of(wrapper);
            } else {
                return getLiteral(strValue);
            }
        }
        // Only gets string values typed by user
        throw new AssertionError();
    }

    private Value getLiteral(String value) {
        // Boolean
        if (value.equals("false")) return Value.of(false);
        if (value.equals("true")) return Value.of(true);

        // Number
        try {
            return Value.of(Double.parseDouble(value));
        } catch (NumberFormatException ignored) {
        }

        // String
        return Value.of(value);
    }

    public JSheetCell resolveRef(String name) {
        Pattern pattern = Pattern.compile("([a-zA-Z]+)(\\d+)");
        Matcher matcher = pattern.matcher(name);
        if (!matcher.matches())
            return null;

        String column = matcher.group(1);
        int columnIndex = findColumn(column);
        if (columnIndex == -1)
            return null;

        String row = matcher.group(2);
        int rowIndex = Integer.parseInt(row);
        if (rowIndex >= getRowCount())
            return null;

        return new JSheetCell(rowIndex, columnIndex);
    }

    /**
     * If {@code cell} contains a formula than its result is already computed
     */
    public Result getResultAt(JSheetCell cell) {
        Value value = getValueAt(cell.row, cell.column);
        String strCell = getColumnName(cell.column) + cell.row;
        if (value == null) {
            return Result.failure(String.format("Cell %s is uninitialized", strCell));
        }
        if (value.getTag() == Value.Type.EXPR) {
            return value.getAsExpr().getResult();
        } else { // Plain value
            return Result.success(value);
        }
    }

    public boolean isModified() {
        return modified;
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }

    private int nonEmptyRowsCount() {
        int lastNonEmpty = -1;
        for (int i = getRowCount() - 1; i >= 0; i--) {
            boolean nonEmpty = Arrays.stream(data.get(i))
                .anyMatch(Objects::nonNull);
            if (nonEmpty) {
                lastNonEmpty = i;
                break;
            }
        }
        return lastNonEmpty + 1;
    }

    /**
     * Deserializes a model from a CSV {@code file}.
     */
    public static JSheetTableModel read(File file) throws IOException, CsvValidationException {
        List<Value[]> data = new ArrayList<>();
        try (var reader = new CSVReader(new FileReader(file))) {
            String[] line;
            while ((line = reader.readNext()) != null) {
                Value[] row = new Value[line.length];
                for (int i = 0; i < line.length; i++) {
                    String strValue = line[i];
                    row[i] = strValue.isEmpty() ? null : Value.of(strValue);
                }
                data.add(row);
            }
        }
        return new JSheetTableModel(data);
    }

    /**
     * Serializes {@code model} in a CSV {@code file}.
     */
    public static void write(File file, JSheetTableModel model) throws IOException {
        try (var writer = new CSVWriter(new FileWriter(file))) {
            for (int row = 0; row < model.nonEmptyRowsCount(); row++) {
                String[] strRow = Arrays.stream(model.data.get(row))
                    .map(o -> o == null ? "" : o.toString())
                    .toArray(String[]::new);
                writer.writeNext(strRow);
            }
        }
    }

    private enum ComputationStage {
        NOT_COMPUTED, IN_PROGRESS, COMPUTED
    }

    private class DependencyManager {
        // Dependency graph
        private final Map<JSheetCell, Collection<JSheetCell>> references = new HashMap<>();
        private final Map<JSheetCell, Collection<JSheetCell>> referencedBy = new HashMap<>();

        // Computation state
        private final Map<JSheetCell, ComputationStage> computationStage = new HashMap<>();

        void addFormula(JSheetCell cell, ExprWrapper formula) {
            Map<String, JSheetCell> refToCell = formula.getRefToCell();
            for (var c : refToCell.values()) {
                addLink(cell, c);
            }
        }

        void removeFormula(JSheetCell cell, ExprWrapper formula) {
            for (var c : formula.getRefToCell().values()) {
                removeLink(cell, c);
            }
        }

        void addLink(JSheetCell from, JSheetCell to) {
            references
                .computeIfAbsent(from, k -> new ArrayList<>())
                .add(to);
            referencedBy
                .computeIfAbsent(to, k -> new ArrayList<>())
                .add(from);
        }

        void removeLink(JSheetCell from, JSheetCell to) {
            references.get(from).remove(to);
            referencedBy.get(to).remove(from);
        }

        /**
         * @return a set of cells that are which transitively depend on {@code cell}.
         */
        Collection<JSheetCell> getDependentOn(JSheetCell cell) {
            Set<JSheetCell> dependent = new HashSet<>();
            if (getValueAt(cell.row, cell.column).getTag() == Value.Type.EXPR)
                dependent.add(cell);
            Queue<JSheetCell> queue = new ArrayDeque<>();
            queue.add(cell);
            while (!queue.isEmpty()) {
                JSheetCell v = queue.remove();
                Collection<JSheetCell> us = referencedBy.get(v);
                if (us == null)
                    continue;
                for (var u : us) {
                    if (!dependent.contains(u)) {
                        queue.add(u);
                        dependent.add(u);
                    }
                }
            }
            return dependent;
        }

        void recomputeAll(JSheetCell changed) {
            // Find all cells that need re-computation and invalidate them
            Collection<JSheetCell> invalid = getDependentOn(changed);
            computationStage.clear();
            for (var cell : invalid) {
                computationStage.put(cell, ComputationStage.NOT_COMPUTED);
            }

            // Recompute all at once in a single DFS traversal
            for (var cell : invalid) {
                if (computationStage.get(cell) == ComputationStage.NOT_COMPUTED)
                    dfs(cell);
            }

            // Fire table changed events
            for (var cell : invalid) {
                fireTableCellUpdated(cell.row, cell.column);
            }
        }

        void dfs(JSheetCell u) {
            computationStage.put(u, ComputationStage.IN_PROGRESS);
            boolean circular = false;
            if (references.containsKey(u)) {
                for (var v : references.get(u)) {
                    if (!computationStage.containsKey(v)) {
                        // v is a plain value or doesn't need re-computation
                        continue;
                    }
                    ComputationStage stage = computationStage.get(v);
                    if (stage == ComputationStage.COMPUTED) {
                        // If v is computed, we can safely get its value
                        // If v is on a cycle, the error will propagate to u
                        continue;
                    }
                    if (stage == ComputationStage.IN_PROGRESS) {
                        // Found a loop
                        circular = true;
                        continue;
                    }
                    if (stage == ComputationStage.NOT_COMPUTED) {
                        dfs(v);
                    }
                }
            }
            ExprWrapper current = getValueAt(u.row, u.column).getAsExpr();
            if (circular) {
                current.setResult(Result.failure("Circular dependency"));
            } else {
                // All of the cells u references are evaluated
                current.eval(JSheetTableModel.this);
            }
            computationStage.put(u, ComputationStage.COMPUTED);
        }
    }
}
