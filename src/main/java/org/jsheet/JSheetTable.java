package org.jsheet;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

public class JSheetTable extends JTable {
    public JSheetTable(TableModel model) {
        super(model);
    }

    @Override
    public TableCellRenderer getCellRenderer(int row, int column) {
        Object value = getValueAt(row, column);
        if (value != null)
            return getDefaultRenderer(value.getClass());
        else
            return super.getCellRenderer(row, column);
    }
}
