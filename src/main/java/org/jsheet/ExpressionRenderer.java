package org.jsheet;

import org.jsheet.model.Result;
import org.jsheet.model.Type;
import org.jsheet.model.Value;

import javax.swing.table.DefaultTableCellRenderer;
import java.text.NumberFormat;

public class ExpressionRenderer extends DefaultTableCellRenderer {
    private final NumberFormat format = NumberFormat.getInstance();
    {
        format.setGroupingUsed(false);
    }

    @Override
    protected void setValue(Object o) {
        Value value = (Value) o;
        if (value.getTag() == Type.FORMULA) {
            Result result = value.getAsFormula().getResult();
            if (!result.isPresent()) {
                setText("!ERROR");
                setToolTipText(result.message());
                return;
            }
            value = result.get();
        }
        setText(renderPlainValue(value));
        setToolTipText(null);
    }

    private String renderPlainValue(Value value) {
        switch (value.getTag()) {
            case BOOLEAN: return String.valueOf(value.getAsBoolean());
            case DOUBLE: return format.format(value.getAsDouble());
            case STRING: return value.getAsString();
            case FORMULA:
            case RANGE:
                throw new AssertionError("should not be rendered");
            default: throw new AssertionError();
        }
    }
}
