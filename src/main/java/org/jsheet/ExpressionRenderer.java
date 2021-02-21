package org.jsheet;

import org.jsheet.model.Result;
import org.jsheet.model.Value;

import javax.swing.table.DefaultTableCellRenderer;

public class ExpressionRenderer extends DefaultTableCellRenderer {
    @Override
    protected void setValue(Object o) {
        Value value = (Value) o;
        if (value.getTag() == Value.Type.EXPR) {
            Result result = value.getAsExpr().getResult();
            if (!result.isPresent()) {
                setText("!ERROR");
                return;
            }
            value = result.get();
        }
        setText(renderPlainValue(value));
    }

    private String renderPlainValue(Value value) {
        switch (value.getTag()) {
            case BOOLEAN: return String.valueOf(value.getAsBoolean());
            case DOUBLE: return String.format("%.2f", value.getAsDouble());
            case STRING: return value.getAsString();
            case EXPR: throw new IllegalArgumentException();
        }
        throw new AssertionError();
    }
}
