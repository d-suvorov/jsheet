package org.jsheet;

import org.jsheet.model.*;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

public class JSheetTable extends JTable {
    private Clipboard clipboard;

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
        clipboard = new Clipboard(
            new Cell(selectedRow, selectedColumn),
            getSelectedRowCount(), getSelectedColumnCount()
        );
    }

    public void paste() {
        int selectedRow = getSelectedRow();
        int selectedColumn = getSelectedColumn();
        if (selectedRow == -1 || selectedColumn == -1)
            return;
        if (clipboard != null)
            clipboard.paste(new Cell(selectedRow, selectedColumn));
    }

    private class Clipboard {
        final Cell source;
        final int rowCount;
        final int columnCount;
        final Value[][] buffer;

        Clipboard(Cell source, int rowCount, int columnCount) {
            this.source = source;
            this.rowCount = rowCount;
            this.columnCount = columnCount;
            this.buffer = new Value[rowCount][columnCount];
            copy();
        }

        void copy() {
            for (int rowOffset = 0; rowOffset < rowCount; rowOffset++) {
                for (int columnOffset = 0; columnOffset < columnCount; columnOffset++) {
                    int srcRow = source.row + rowOffset;
                    int srcColumn = source.column + columnOffset;
                    Value value = (Value) getValueAt(srcRow, srcColumn);
                    buffer[rowOffset][columnOffset] = value;
                }
            }
        }

        void paste(Cell destination) {
            JSheetTableModel model = (JSheetTableModel) getModel();
            for (int rowOffset = 0; rowOffset < rowCount; rowOffset++) {
                int dstRow = destination.row + rowOffset;
                if (dstRow >= model.getRowCount())
                    break;
                for (int columnOffset = 0; columnOffset < columnCount; columnOffset++) {
                    int dstColumn = destination.column + columnOffset;
                    if (dstColumn >= model.getColumnCount())
                        break;
                    Value value = buffer[rowOffset][columnOffset];
                    if (value.getTag() == Type.FORMULA) {
                        int rowShift = destination.row - source.row;
                        int columnShift = destination.column - source.column;
                        Formula shifted = value.getAsFormula().shift(model, rowShift, columnShift);
                        setValueAt(Value.of(shifted), dstRow, dstColumn);
                    } else {
                        // Plain values are immutable so it's fine
                        // to have two references on the same object
                        setValueAt(value, dstRow, dstColumn);
                    }
                }
            }
        }
    }
}
