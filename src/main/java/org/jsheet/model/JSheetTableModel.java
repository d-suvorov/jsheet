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
    private static final int DEFAULT_ROW_COUNT = 100;
    private static final int DEFAULT_COLUMN_COUNT = 26;

    private static final Pattern REF_PATTERN = Pattern.compile("([a-zA-Z]+)(\\d+)");

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
     * Takes a raw {@code data} of string values and tries to reinsert them as model values.
     */
    private JSheetTableModel(List<Value[]> data) {
        this.data = data;
        for (int row = 0; row < getRowCount(); row++) {
            for (int column = 0; column < getColumnCount(); column++) {
                Value value = getValueAt(row, column);
                // tries to parse functions and numbers from strings
                setValueAt(value == null ? null : value.getAsString(), row, column);
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

    private boolean containsCell(int rowIndex, int columnIndex) {
        return rowIndex >= 0 && rowIndex < getRowCount()
            && columnIndex >= 0 && columnIndex < getColumnCount();
    }

    @Override
    public Value getValueAt(int rowIndex, int columnIndex) {
        if (!containsCell(rowIndex, columnIndex))
            throw new IllegalArgumentException("out of bounds");
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
        Cell current = new Cell(rowIndex, columnIndex);
        Value prev = getValueAt(rowIndex, columnIndex);
        if (prev != null && prev.getTag() == Type.EXPRESSION) {
            ExprWrapper wrapper = prev.getAsExpression();
            dependencies.removeFormula(current, wrapper);
        }
        data.get(rowIndex)[columnIndex] = getModelValue(value, current);
        dependencies.recomputeAll(current);
        fireTableCellUpdated(rowIndex, columnIndex);
    }

    private Value getModelValue(Object object, Cell current) {
        if (object == null)
            return null;
        if (object instanceof Value) {
            Value value = (Value) object;
            if (value.getTag() == Type.EXPRESSION) {
                dependencies.addFormula(current, value.getAsExpression());
            }
            return value;
        }
        if (object instanceof String) {
            String strValue = (String) object;
            if (strValue.startsWith("=")) {
                ExprWrapper formula = ParserUtils.parse(strValue);
                if (formula.isParsed())
                    formula.resolveRefs(this);
                dependencies.addFormula(current, formula);
                return Value.of(formula);
            } else {
                return getLiteral(strValue);
            }
        }
        // Only gets string values typed by user or model values pasted from other cells
        throw new AssertionError();
    }

    private Value getLiteral(String strValue) {
        // Boolean
        if (strValue.equals("false")) return Value.of(false);
        if (strValue.equals("true")) return Value.of(true);

        // Number
        try {
            return Value.of(Double.parseDouble(strValue));
        } catch (NumberFormatException ignored) {}

        // String
        return Value.of(strValue);
    }

    public Cell resolveRef(String name) {
        Matcher matcher = REF_PATTERN.matcher(name);
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

        return new Cell(rowIndex, columnIndex);
    }

    /**
     * If {@code cell} contains a formula than its result is already computed
     */
    public Result getResultAt(Cell cell) {
        Value value = getValueAt(cell.row, cell.column);
        String strCell = getColumnName(cell.column) + cell.row;
        if (value == null) {
            return Result.failure(String.format("Cell %s is uninitialized", strCell));
        }
        if (value.getTag() == Type.EXPRESSION) {
            return value.getAsExpression().getResult();
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
            for (int row = 0; row < model.getRowCount(); row++) {
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
        private final Map<Cell, Collection<Cell>> references = new HashMap<>();
        private final Map<Cell, Collection<Cell>> referencedBy = new HashMap<>();

        // Computation state
        private final Map<Cell, ComputationStage> computationStage = new HashMap<>();

        void addFormula(Cell cell, ExprWrapper formula) {
            if (!formula.isParsed())
                return;
            for (var ref : formula.getRefs()) {
                if (ref.isResolved())
                    addLink(cell, ref.getCell());
            }
            for (var range : formula.getRanges()) {
                if (range.isResolved()) {
                    for (var c : range) {
                        addLink(cell, c);
                    }
                }
            }
        }

        void removeFormula(Cell cell, ExprWrapper formula) {
            if (!formula.isParsed())
                return;
            for (var ref : formula.getRefs()) {
                if (ref.isResolved())
                    removeLink(cell, ref.getCell());
            }
            for (var range : formula.getRanges()) {
                if (range.isResolved()) {
                    for (var c : range) {
                        removeLink(cell, c);
                    }
                }
            }
        }

        void addLink(Cell from, Cell to) {
            references
                .computeIfAbsent(from, k -> new ArrayList<>())
                .add(to);
            referencedBy
                .computeIfAbsent(to, k -> new ArrayList<>())
                .add(from);
        }

        void removeLink(Cell from, Cell to) {
            references.get(from).remove(to);
            referencedBy.get(to).remove(from);
        }

        /**
         * @return a set of cells that are which transitively depend on {@code cell}.
         */
        Collection<Cell> getDependentOn(Cell cell) {
            Set<Cell> dependent = new HashSet<>();
            Value cellValue = getValueAt(cell.row, cell.column);
            if (cellValue != null && cellValue.getTag() == Type.EXPRESSION)
                dependent.add(cell);
            Queue<Cell> queue = new ArrayDeque<>();
            queue.add(cell);
            while (!queue.isEmpty()) {
                Cell v = queue.remove();
                Collection<Cell> us = referencedBy.get(v);
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

        void recomputeAll(Cell changed) {
            // Find all cells that need re-computation and invalidate them
            Collection<Cell> invalid = getDependentOn(changed);
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

        void dfs(Cell u) {
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
            ExprWrapper current = getValueAt(u.row, u.column).getAsExpression();
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
