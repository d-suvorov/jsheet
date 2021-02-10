package org.jsheet;

import org.jsheet.parser.ExprWrapper;

import javax.swing.table.DefaultTableCellRenderer;

public class ExpressionRenderer extends DefaultTableCellRenderer {
    @Override
    protected void setValue(Object value) {
        ExprWrapper wrapper = (ExprWrapper) value;
        double res = wrapper.expression.eval();
        setText(String.format("%.2f", res));
    }
}
