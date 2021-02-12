package org.jsheet.model;

import org.jsheet.expr.Expr;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExprWrapper {
    public final String definition;
    public final Expr expression;

    private boolean computed = false;
    private Result result;

    private final List<String> refs;
    private Map<String, JSheetCell> refToCell;

    public ExprWrapper(String definition, Expr expression, List<String> refs) {
        this.definition = definition;
        this.expression = expression;
        this.refs = refs;
    }

    public Map<String, JSheetCell> getRefToCell() {
        return Collections.unmodifiableMap(refToCell);
    }

    @Override
    public String toString() {
        return definition;
    }

    public void invalidate() {
        computed = false;
    }

    public Result eval(JSheetTableModel model) {
        if (computed)
            return result;
        if (refToCell == null)
            resolveRefs(model);
        computed = true;
        result = expression.eval(model, refToCell);
        return result;
    }

    void resolveRefs(JSheetTableModel model) {
        if (refToCell == null) {
            refToCell = new HashMap<>();
        }
        for (String ref : refs) {
            JSheetCell cell = model.resolveRef(ref);
            if (cell != null)
                refToCell.put(ref, cell);
        }
    }
}
