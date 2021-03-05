package org.jsheet;

import org.jsheet.evaluation.Result;
import org.jsheet.evaluation.Type;
import org.jsheet.evaluation.Value;

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
            case BOOLEAN: return value.getAsBoolean().toString();
            case DOUBLE: return format.format(value.getAsDouble());
            case STRING: return value.getAsString();
            case RANGE: return value.getAsRange().getName();
            case FORMULA:
                throw new AssertionError("should not be rendered");
            default: throw new AssertionError();
        }
    }
}
