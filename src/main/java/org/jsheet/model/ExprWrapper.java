package org.jsheet.model;

import org.jsheet.model.expr.Expr;
import org.jsheet.model.expr.Ref;

import java.util.Collections;
import java.util.List;

public class ExprWrapper {
    public final String originalDefinition;
    public final Expr expression;

    private Result result;

    private final List<Ref> refs;

    public ExprWrapper(String originalDefinition, Expr expression, List<Ref> refs) {
        this.originalDefinition = originalDefinition;
        this.expression = expression;
        this.refs = refs;
    }

    public List<Ref> getRefs() {
        return Collections.unmodifiableList(refs);
    }

    public Result eval(JSheetTableModel model) {
        result = expression.eval(model);
        return result;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    void resolveRefs(JSheetTableModel model) {
        refs.forEach(r -> r.resolve(model));
    }

    @Override
    public String toString() {
        return originalDefinition;
    }
}
