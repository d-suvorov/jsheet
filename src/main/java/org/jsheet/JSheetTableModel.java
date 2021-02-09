package org.jsheet;

import javax.swing.table.AbstractTableModel;

public class JSheetTableModel extends AbstractTableModel {
    private static final int DEFAULT_ROW_COUNT = 10;
    private static final int DEFAULT_COLUMN_COUNT = 10;

    private final double[][] data = new double[DEFAULT_ROW_COUNT][DEFAULT_COLUMN_COUNT];

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
}
