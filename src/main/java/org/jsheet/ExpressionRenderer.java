package org.jsheet;

import org.jsheet.model.ExprWrapper;
import org.jsheet.model.Result;

import javax.swing.table.DefaultTableCellRenderer;

public class ExpressionRenderer extends DefaultTableCellRenderer {
    @Override
    protected void setValue(Object value) {
        ExprWrapper wrapper = (ExprWrapper) value;
        Result result = wrapper.getResult();
        if (result.isPresent()) {
            setText(String.format("%.2f", result.get()));
        } else {
            // TODO think of a nice way to show error message
            setText("!ERROR");
        }
    }
}
