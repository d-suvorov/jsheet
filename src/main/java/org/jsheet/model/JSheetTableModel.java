package org.jsheet.model;

import org.jsheet.parser.ParserUtils;

import javax.swing.table.AbstractTableModel;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JSheetTableModel extends AbstractTableModel {
    private static final int DEFAULT_ROW_COUNT = 10;
    private static final int DEFAULT_COLUMN_COUNT = 10;

    private final Object[][] data = new Object[DEFAULT_ROW_COUNT][DEFAULT_COLUMN_COUNT];

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

    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        data[rowIndex][columnIndex] = getModelValue(value);
    }

    private Object getModelValue(Object value) {
        if (value instanceof String) {
            String strValue = (String) value;
            if (strValue.startsWith("=")) {
                return ParserUtils.parse(strValue.substring(1));
            } else {
                return getLiteral(value, strValue);
            }
        }
        throw new AssertionError();
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

    public double eval(JSheetCell cell) {
        Object value = getValueAt(cell.row, cell.column);
        if (value instanceof ExprWrapper) {
            return ((ExprWrapper) value).eval(this);
        } else if (value instanceof Double) {
            return (Double) value;
        }
        throw new AssertionError("unimplemented");
    }
}
