package org.jsheet.model;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

/**
 * This wrapper is used to display row numbers of a table since
 * {@link javax.swing.JTable} does not support this. I really hope
 * this won't be the cause of infamous off-by-one error.
 */
public class RowNumberedTableModel implements TableModel {
    private final TableModel base;

    public RowNumberedTableModel(TableModel base) {
        this.base = base;
    }

    private int baseColumnIndex(int columnIndex) {
        return columnIndex - 1;
    }

    @Override
    public int getRowCount() {
        return base.getRowCount() + 1;
    }

    @Override
    public int getColumnCount() {
        return base.getColumnCount();
    }

    @Override
    public String getColumnName(int columnIndex) {
        return base.getColumnName(baseColumnIndex(columnIndex));
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return base.getColumnClass(baseColumnIndex(columnIndex));
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (columnIndex == 0)
            return false;
        return base.isCellEditable(rowIndex, baseColumnIndex(columnIndex));
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (columnIndex == 0) {
            return rowIndex;
        }
        return null;
    }

    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        if (columnIndex == 0)
            throw new AssertionError();
        base.setValueAt(value, rowIndex, baseColumnIndex(columnIndex));
    }

    @Override
    public void addTableModelListener(TableModelListener l) {
        base.addTableModelListener(l);
    }

    @Override
    public void removeTableModelListener(TableModelListener l) {
        base.addTableModelListener(l);
    }
}
