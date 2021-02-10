package org.jsheet;

import org.jsheet.expr.Expr;

import javax.swing.table.DefaultTableCellRenderer;

public class ExpressionRenderer extends DefaultTableCellRenderer {
    @Override
    protected void setValue(Object value) {
        double res = ((Expr) value).eval();
        setText(String.format("%.2f", res));
    }
}
