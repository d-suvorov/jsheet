package org.jsheet;

import org.jsheet.model.ExprWrapper;
import org.jsheet.model.JSheetTableModel;
import org.jsheet.model.Result;

import javax.swing.table.DefaultTableCellRenderer;

public class ExpressionRenderer extends DefaultTableCellRenderer {
    private final JSheetTableModel model;

    public ExpressionRenderer(JSheetTableModel model) {
        this.model = model;
    }

    @Override
    protected void setValue(Object value) {
        ExprWrapper wrapper = (ExprWrapper) value;
        Result result = wrapper.eval(model);
        if (result.isPresent()) {
            setText(String.format("%.2f", result.get()));
        } else {
            // TODO think of a nice way to show error message
            setText("!ERROR");
        }
    }
}
