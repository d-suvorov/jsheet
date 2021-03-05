package org.jsheet;

import org.jsheet.data.*;
import org.jsheet.evaluation.Type;
import org.jsheet.evaluation.Value;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

public class JSheetTable extends JTable {
    private Clipboard clipboard;

    public JSheetTable(TableModel model) {
        super(model);
        setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        setCellSelectionEnabled(true);
        getTableHeader().setReorderingAllowed(false);
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
        for (int rowOffset = 0; rowOffset < rowCount; rowOffset++) {
            for (int colOffset = 0; colOffset < columnCount; colOffset++) {
                setValueAt(null, selectedRow + rowOffset, selectedColumn + colOffset);
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
        if (getSelectedRow() == -1 || getSelectedColumn() == -1)
            return;
        if (clipboard != null)
            clipboard.paste();
    }

    private class Clipboard {
        final Cell origin;
        final int rowCount;
        final int columnCount;
        final Value[][] buffer;
        final JSheetTableModel model;

        Clipboard(Cell origin, int rowCount, int columnCount) {
            this.origin = origin;
            this.rowCount = rowCount;
            this.columnCount = columnCount;
            this.buffer = new Value[rowCount][columnCount];
            this.model = (JSheetTableModel) getModel();
            copy();
        }

        void copy() {
            for (int rowOffset = 0; rowOffset < rowCount; rowOffset++) {
                for (int colOffset = 0; colOffset < columnCount; colOffset++) {
                    int srcRow = origin.row + rowOffset;
                    int srcColumn = origin.column + colOffset;
                    Value value = (Value) getValueAt(srcRow, srcColumn);
                    buffer[rowOffset][colOffset] = value;
                }
            }
        }

        void paste() {
            if (rowCount == 1 && columnCount == 1) {
                fillWithSingleCell();
            } else {
                pasteRange();
            }
        }

        private void fillWithSingleCell() {
            for (int rowOffset = 0; rowOffset < getSelectedRowCount(); rowOffset++) {
                for (int colOffset = 0; colOffset < getSelectedColumnCount(); colOffset++) {
                    paste(0, 0, getSelectedRow() + rowOffset, getSelectedColumn() + colOffset);
                }
            }
        }

        private void pasteRange() {
            for (int rowOffset = 0; rowOffset < rowCount; rowOffset++) {
                int dstRow = getSelectedRow() + rowOffset;
                if (dstRow >= model.getRowCount())
                    break;
                for (int colOffset = 0; colOffset < columnCount; colOffset++) {
                    int dstColumn = getSelectedColumn() + colOffset;
                    if (dstColumn >= model.getColumnCount())
                        break;
                    paste(rowOffset, colOffset, dstRow, dstColumn);
                }
            }
        }

        void paste(int bufferRow, int bufferColumn, int dstRow, int dstColumn) {
            Value value = buffer[bufferRow][bufferColumn];
            if (value.getTag() == Type.FORMULA) {
                int rowShift = dstRow - (origin.row + bufferRow);
                int columnShift = dstColumn - (origin.column + bufferColumn);
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
