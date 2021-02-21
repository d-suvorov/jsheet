package org.jsheet.model;

import org.jsheet.expr.Expr;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExprWrapper {
    public final String originalDefinition;
    public final Expr expression;

    private Result result;

    private final List<String> refs;
    private Map<String, JSheetCell> refToCell;

    public ExprWrapper(String originalDefinition, Expr expression, List<String> refs) {
        this.originalDefinition = originalDefinition;
        this.expression = expression;
        this.refs = refs;
    }

    public Map<String, JSheetCell> getRefToCell() {
        return Collections.unmodifiableMap(refToCell);
    }

    public Result eval(JSheetTableModel model) {
        if (refToCell == null)
            resolveRefs(model);
        result = expression.eval(model, refToCell);
        return result;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
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

    @Override
    public String toString() {
        return originalDefinition;
    }
}
