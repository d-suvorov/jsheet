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

    // TODO circular dependencies
    private final Map<JSheetCell, Collection<JSheetCell>> referencedBy = new HashMap<>();

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
        JSheetCell current = new JSheetCell(rowIndex, columnIndex);
        Object prev = getValueAt(rowIndex, columnIndex);
        if (prev instanceof ExprWrapper) {
            ExprWrapper wrapper = (ExprWrapper) prev;
            for (var c : wrapper.getRefToCell().values()) {
                referencedBy.get(c).remove(current);
            }
        }
        data.get(rowIndex)[columnIndex] = getModelValue(value, current);
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
                Map<String, JSheetCell> refToCell = wrapper.getRefToCell();
                for (var c : refToCell.values()) {
                    referencedBy
                        .computeIfAbsent(c, k -> new ArrayList<>())
                        .add(current);
                }
                return wrapper;
            } else {
                return getLiteral(strValue);
            }
        }
        throw new AssertionError();
    }

    private void invalidateReferencingCurrent(JSheetCell current) {
        Collection<JSheetCell> direct = referencedBy.get(current);
        if (direct == null)
            return;

        Set<JSheetCell> invalid = new HashSet<>();
        Queue<JSheetCell> queue = new ArrayDeque<>(direct);
        while (!queue.isEmpty()) {
            JSheetCell cell = queue.remove();
            invalid.add(cell);
            Collection<JSheetCell> cells = referencedBy.get(cell);
            if (cells == null)
                continue;
            for (var c : cells) {
                if (!invalid.contains(c)) {
                    queue.add(c);
                }
            }
        }

        for (var c : invalid) {
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
}
