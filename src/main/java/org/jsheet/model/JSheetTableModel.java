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

    private final List<Object[]> data;

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
            data.add(new Object[columnCount]);
        }
    }

    /**
     * Takes a raw {@code data} of strings and tries to reinsert them as model values.
     */
    private JSheetTableModel(List<Object[]> data) {
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
    public Object getValueAt(int rowIndex, int columnIndex) {
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
        Object prev = getValueAt(rowIndex, columnIndex);
        if (prev instanceof ExprWrapper) {
            ExprWrapper wrapper = (ExprWrapper) prev;
            dependencies.removeFormula(current, wrapper);
        }
        data.get(rowIndex)[columnIndex] = getModelValue(value, current);
        dependencies.recalculateDependencies();
        invalidateReferencingCurrent(current);
    }

    private Object getModelValue(Object value, JSheetCell current) {
        if (value == null)
            return null;
        if (value instanceof String) {
            String strValue = (String) value;
            if (strValue.startsWith("=")) {
                ExprWrapper wrapper = ParserUtils.parse(strValue);
                wrapper.resolveRefs(this);
                dependencies.addFormula(current, wrapper);
                return wrapper;
            } else {
                return getLiteral(strValue);
            }
        }
        throw new AssertionError();
    }

    private void invalidateReferencingCurrent(JSheetCell current) {
        for (var c : dependencies.getDependentOn(current)) {
            ExprWrapper wrapper = (ExprWrapper) getValueAt(c.row, c.column);
            wrapper.invalidate();
            fireTableCellUpdated(c.row, c.column);
        }
    }

    private Object getLiteral(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return value;
        }
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

    public Result eval(JSheetCell cell) {
        if (dependencies.inCircular(cell))
            return Result.failure("Circular dependency");

        Object value = getValueAt(cell.row, cell.column);
        String strCell = getColumnName(cell.column) + cell.row;
        if (value == null) {
            return Result.failure(String.format("Cell %s is uninitialized", strCell));
        }
        if (value instanceof ExprWrapper) {
            return ((ExprWrapper) value).eval(this);
        } else if (value instanceof Number) {
            return Result.success(((Number) value).doubleValue());
        }
        return Result.failure(String.format("Wrong value type in the cell %s", strCell));
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
        List<Object[]> data = new ArrayList<>();
        try (var reader = new CSVReader(new FileReader(file))) {
            String[] line;
            while ((line = reader.readNext()) != null) {
                Object[] row = new Object[line.length];
                for (int i = 0; i < line.length; i++) {
                    String value = line[i];
                    row[i] = value.isEmpty() ? null : value;
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

    private class DependencyManager {
        List<JSheetCell> formulae = new ArrayList<>();

        // Dependency graph
        private final Map<JSheetCell, Collection<JSheetCell>> references = new HashMap<>();
        private final Map<JSheetCell, Collection<JSheetCell>> referencedBy = new HashMap<>();

        // Strongly connected components mappings
        private final Map<Integer, Collection<JSheetCell>> components = new HashMap<>();
        private final Map<JSheetCell, Integer> cellToComponent = new HashMap<>();
        // Cells that cannot be evaluated due to circular dependencies
        private final Set<JSheetCell> circular = new HashSet<>();

        // Strongly connected components computation algorithm state
        Set<JSheetCell> visited = new HashSet<>();
        List<JSheetCell> order = new ArrayList<>();
        int currentComponent;

        void addFormula(JSheetCell cell, ExprWrapper formula) {
            formulae.add(cell);
            Map<String, JSheetCell> refToCell = formula.getRefToCell();
            for (var c : refToCell.values()) {
                addLink(cell, c);
            }
        }

        void removeFormula(JSheetCell cell, ExprWrapper formula) {
            formulae.remove(cell);
            for (var c : formula.getRefToCell().values()) {
                removeLink(cell, c);
            }
        }

        void recalculateDependencies() {
            recalculateComponents();
            recalculateCircular();
            printDebug();
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
            Collection<JSheetCell> direct = referencedBy.get(cell);
            if (direct == null)
                return Collections.emptySet();
            return getDependentOn(direct);
        }

        /**
         * @return a set of cells that are which transitively depend on {@code cells}.
         */
        Collection<JSheetCell> getDependentOn(Collection<JSheetCell> cells) {
            Set<JSheetCell> dependent = new HashSet<>();
            Queue<JSheetCell> queue = new ArrayDeque<>(cells);
            while (!queue.isEmpty()) {
                JSheetCell v = queue.remove();
                dependent.add(v);
                Collection<JSheetCell> us = referencedBy.get(v);
                if (us == null)
                    continue;
                for (var u : us) {
                    if (!dependent.contains(u)) {
                        queue.add(u);
                    }
                }
            }
            return dependent;
        }

        void recalculateComponents() {
            int n = formulae.size();

            components.clear();
            cellToComponent.clear();
            order.clear();
            visited.clear();

            for (var u : formulae) {
                if (!visited.contains(u)) {
                    dfs1(u);
                }
            }

            currentComponent = 0;
            for (int i = n - 1; i >= 0; i--) {
                JSheetCell u = order.get(i);
                if (!cellToComponent.containsKey(u)) {
                    dfs2(u);
                    currentComponent++;
                }
            }
        }

        private void printDebug() {
            System.out.println("SCCs");
            for (var e : components.entrySet()) {
                System.out.print(e.getKey() + ": {");
                for (var u : e.getValue()) {
                    System.out.print(u + ", ");
                }
                System.out.println("}");
            }
            System.out.println("====");
            System.out.println("Circular: {");
            for (var u : circular) {
                System.out.print(u + " ");
            }
            System.out.println("}");
            System.out.println("====");
        }

        void recalculateCircular() {
            circular.clear();
            for (var e : components.entrySet()) {
                if (e.getValue().size() > 1) {
                    circular.addAll(e.getValue());
                }
            }
            circular.addAll(getDependentOn(circular));
        }

        boolean inCircular(JSheetCell cell) {
            return circular.contains(cell);
        }

        void dfs1(JSheetCell u) {
            visited.add(u);
            Collection<JSheetCell> cells = references.get(u);
            if (cells != null) {
                for (var v : cells) {
                    // Discard links to plain values
                    if (!(getValueAt(v.row, v.column) instanceof ExprWrapper))
                        continue;
                    if (!visited.contains(v))
                        dfs1(v);
                }
            }
            order.add(u);
        }

        void dfs2(JSheetCell u) {
            components
                .computeIfAbsent(currentComponent, k -> new ArrayList<>())
                .add(u);
            cellToComponent.put(u, currentComponent);
            Collection<JSheetCell> cells = references.get(u);
            if (cells != null) {
                for (var v : cells) {
                    // Discard links to plain values
                    if (!(getValueAt(v.row, v.column) instanceof ExprWrapper))
                        continue;
                    if (!cellToComponent.containsKey(v))
                        dfs2(v);
                }
            }
        }
    }
}
