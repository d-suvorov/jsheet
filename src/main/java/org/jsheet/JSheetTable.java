package org.jsheet;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

public class JSheetTable extends JTable {
    private Object[][] clipboard;

    public JSheetTable(TableModel model) {
        super(model);
        setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        setCellSelectionEnabled(true);
    }

    @Override
    public TableCellRenderer getCellRenderer(int row, int column) {
        Object value = getValueAt(row, column);
        if (value != null)
            return getDefaultRenderer(value.getClass());
        else
            return super.getCellRenderer(row, column);
    }

    public void delete() {
        int selectedRow = getSelectedRow();
        int selectedColumn = getSelectedColumn();
        if (selectedRow == -1 || selectedColumn == -1)
            return;
        int rowCount = getSelectedRowCount();
        int columnCount = getSelectedColumnCount();
        for (int row = selectedRow; row < rowCount; row++) {
            for (int column = selectedColumn; column < columnCount; column++) {
                setValueAt(null, row, column);
            }
        }
    }

    public void cut() {
        copy();
        delete();
    }

    public void copy() {
        int selectedRow = getSelectedRow();
        int selectedColumn = getSelectedColumn();
        if (selectedRow == -1 || selectedColumn == -1)
            return;
        clipboard = new Object[getSelectedRowCount()][getSelectedColumnCount()];
        for (int rowOffset = 0; rowOffset < getSelectedRowCount(); rowOffset++) {
            for (int columnOffset = 0; columnOffset < getSelectedColumnCount(); columnOffset++) {
                int srcRow = selectedRow + rowOffset;
                int srcColumn = selectedColumn + columnOffset;
                Object value = getValueAt(srcRow, srcColumn);
                clipboard[rowOffset][columnOffset] = value;
            }
        }
    }

    public void paste() {
        int selectedRow = getSelectedRow();
        int selectedColumn = getSelectedColumn();
        if (selectedRow == -1 || selectedColumn == -1)
            return;

        TableModel model = getModel();
        for (int row = 0; row < clipboard.length; row++) {
            int dstRow = selectedRow + row;
            if (dstRow >= model.getRowCount())
                break;
            for (int column = 0; column < clipboard[0].length; column++) {
                int dstColumn = selectedColumn + column;
                if (dstColumn >= model.getColumnCount())
                    break;
                setValueAt(clipboard[row][column], dstRow, dstColumn);
            }
        }
    }
}
