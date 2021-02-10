package org.jsheet;

import org.jsheet.parser.ParserUtils;

import javax.swing.table.AbstractTableModel;

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
            }
        }
        return value;
    }
}
