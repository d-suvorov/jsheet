package org.jsheet;

import org.jsheet.model.ExprWrapper;
import org.jsheet.model.JSheetTableModel;

import javax.swing.table.DefaultTableCellRenderer;

public class ExpressionRenderer extends DefaultTableCellRenderer {
    private final JSheetTableModel model;

    public ExpressionRenderer(JSheetTableModel model) {
        this.model = model;
    }

    @Override
    protected void setValue(Object value) {
        ExprWrapper wrapper = (ExprWrapper) value;
        double res = wrapper.eval(model);
        setText(String.format("%.2f", res));
    }
}
