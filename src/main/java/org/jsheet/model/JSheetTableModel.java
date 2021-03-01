package org.jsheet.model;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import org.jsheet.parser.ParseException;

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

    private static final Pattern REFERENCE_PATTERN = Pattern.compile("([a-zA-Z]+)(\\d+)");

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
     * Constructs a model from a raw {@code data} of values.
     */
    private JSheetTableModel(List<Value[]> data) {
        this.data = data;
        for (int row = 0; row < getRowCount(); row++) {
            for (int column = 0; column < getColumnCount(); column++) {
                Value value = getValueAt(row, column);
                if (value != null && value.getTag() == Type.FORMULA) {
                    Formula formula = value.getAsFormula();
                    formula.resolveReferences(this);
                    Cell current = new Cell(row, column);
                    dependencies.addFormula(current, formula);
                    dependencies.reevaluateAll(current);
                }
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
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        setModified(true);
        Cell current = new Cell(rowIndex, columnIndex);
        Value prev = getValueAt(rowIndex, columnIndex);
        if (prev != null && prev.getTag() == Type.FORMULA) {
            Formula formula = prev.getAsFormula();
            dependencies.removeFormula(current, formula);
        }
        Value value = (Value) aValue;
        if (value != null && value.getTag() == Type.FORMULA) {
            Formula formula = value.getAsFormula();
            formula.resolveReferences(this);
            dependencies.addFormula(current, formula);
        }
        data.get(rowIndex)[columnIndex] = value;
        dependencies.reevaluateAll(current);
        fireTableCellUpdated(rowIndex, columnIndex);
    }

    public Cell resolveReference(String name) {
        Matcher matcher = REFERENCE_PATTERN.matcher(name);
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
     * If {@code cell} contains a formula than its result is already evaluated
     */
    public Result getResultAt(Cell cell) {
        Value value = getValueAt(cell.row, cell.column);
        String strCell = getColumnName(cell.column) + cell.row;
        if (value == null) {
            return Result.failure(String.format("Cell %s is uninitialized", strCell));
        }
        if (value.getTag() == Type.FORMULA) {
            return value.getAsFormula().getResult();
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
    public static JSheetTableModel read(File file)
        throws IOException, CsvValidationException, ParseException
    {
        List<Value[]> data = new ArrayList<>();
        try (var reader = new CSVReader(new FileReader(file))) {
            String[] line;
            while ((line = reader.readNext()) != null) {
                Value[] row = new Value[line.length];
                for (int i = 0; i < line.length; i++) {
                    String strValue = line[i];
                    row[i] = strValue.isEmpty() ? null : Value.parse(strValue);
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

    private enum EvaluationStage {
        NOT_EVALUATED, IN_PROGRESS, EVALUATED
    }

    private class DependencyManager {
        // Dependency graph
        private final Map<Cell, Collection<Cell>> references = new HashMap<>();
        private final Map<Cell, Collection<Cell>> referencedBy = new HashMap<>();

        // Computation state
        private final Map<Cell, EvaluationStage> evaluationStage = new HashMap<>();

        void addFormula(Cell cell, Formula formula) {
            for (var ref : formula.getReferences()) {
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

        void removeFormula(Cell cell, Formula formula) {
            for (var ref : formula.getReferences()) {
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
            if (cellValue != null && cellValue.getTag() == Type.FORMULA)
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

        void reevaluateAll(Cell changed) {
            // Find all cells that need re-computation and invalidate them
            Collection<Cell> invalid = getDependentOn(changed);
            evaluationStage.clear();
            for (var cell : invalid) {
                evaluationStage.put(cell, EvaluationStage.NOT_EVALUATED);
            }

            // Re-evaluate all at once in a single DFS traversal
            for (var cell : invalid) {
                if (evaluationStage.get(cell) == EvaluationStage.NOT_EVALUATED)
                    dfs(cell);
            }

            // Fire table changed events
            for (var cell : invalid) {
                fireTableCellUpdated(cell.row, cell.column);
            }
        }

        void dfs(Cell u) {
            evaluationStage.put(u, EvaluationStage.IN_PROGRESS);
            boolean circular = false;
            if (references.containsKey(u)) {
                for (var v : references.get(u)) {
                    if (!evaluationStage.containsKey(v)) {
                        // v is a plain value or doesn't need re-computation
                        continue;
                    }
                    EvaluationStage stage = evaluationStage.get(v);
                    if (stage == EvaluationStage.EVALUATED) {
                        // If v is evaluated, we can safely get its value
                        // If v is on a cycle, the error will propagate to u
                        continue;
                    }
                    if (stage == EvaluationStage.IN_PROGRESS) {
                        // Found a loop
                        circular = true;
                        continue;
                    }
                    if (stage == EvaluationStage.NOT_EVALUATED) {
                        dfs(v);
                    }
                }
            }
            Formula current = getValueAt(u.row, u.column).getAsFormula();
            if (circular) {
                current.setResult(Result.failure("Circular dependency"));
            } else {
                // All of the cells u references are evaluated
                current.eval(JSheetTableModel.this);
            }
            evaluationStage.put(u, EvaluationStage.EVALUATED);
        }
    }
}
