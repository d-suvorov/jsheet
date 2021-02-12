package org.jsheet.model;

import org.jsheet.parser.ParserUtils;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JSheetTableModel extends AbstractTableModel {
    private static final int DEFAULT_ROW_COUNT = 10;
    private static final int DEFAULT_COLUMN_COUNT = 10;

    private final Object[][] data = new Object[DEFAULT_ROW_COUNT][DEFAULT_COLUMN_COUNT];

    // TODO circular dependencies
    private final Map<JSheetCell, Collection<JSheetCell>> referencedBy = new HashMap<>();

    @Override
    public int getRowCount() {
        return data.length;
    }

    @Override
    public int getColumnCount() {
        return data[0].length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return data[rowIndex][columnIndex];
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return true;
    }

    /**
     * 1. If a current value is a formula, removes links to cells {@code referencedBy} it.
     * 2. If a new value is a formula, adds links to cells {@code referencedBy} it.
     * 3. Invalidate formulae results current cell is {@code referencedBy}.
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
        data[rowIndex][columnIndex] = getModelValue(value, current);
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
                return getLiteral(value, strValue);
            }
        }
        throw new AssertionError();
    }

    private void invalidateReferencingCurrent(JSheetCell current) {
        Collection<JSheetCell> cells = referencedBy.get(current);
        if (cells == null)
            return;
        for (var c : cells) {
            ExprWrapper wrapper = (ExprWrapper) getValueAt(c.row, c.column);
            wrapper.invalidate();
            fireTableCellUpdated(c.row, c.column);
        }
    }

    private Object getLiteral(Object value, String strValue) {
        try {
            return Double.parseDouble(strValue);
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
}
