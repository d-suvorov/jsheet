package org.jsheet.model;

import org.jsheet.expr.Expr;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExprWrapper {
    public final String definition;
    public final Expr expression;

    private final List<String> refs;
    private Map<String, JSheetCell> refToCell;

    public ExprWrapper(String definition, Expr expression, List<String> refs) {
        this.definition = definition;
        this.expression = expression;
        this.refs = refs;
    }

    @Override
    public String toString() {
        return definition;
    }

    public double eval(JSheetTableModel model) {
        if (!refs.isEmpty())
            resolveRefs(model);
        return expression.eval(model, refToCell);
    }

    private void resolveRefs(JSheetTableModel model) {
        if (refToCell == null) {
            refToCell = new HashMap<>();
        } else {
            refToCell.clear();
        }
        for (String ref : refs) {
            JSheetCell cell = model.resolveRef(ref);
            if (cell != null)
                refToCell.put(ref, cell);
        }
    }
}
