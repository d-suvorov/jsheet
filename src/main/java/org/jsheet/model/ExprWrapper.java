package org.jsheet.model;

import org.jsheet.model.expr.Expr;
import org.jsheet.model.expr.Range;
import org.jsheet.model.expr.Ref;

import java.util.Collections;
import java.util.Set;

public class ExprWrapper {
    public final String originalDefinition;
    public final Expr expression;

    private Result result;

    private final Set<Ref> refs;
    private final Set<Range> ranges;

    public ExprWrapper(String originalDefinition, Expr expression,
        Set<Ref> refs, Set<Range> ranges)
    {
        this.originalDefinition = originalDefinition;
        this.expression = expression;
        this.refs = refs;
        this.ranges = ranges;
    }

    public Set<Ref> getRefs() {
        return Collections.unmodifiableSet(refs);
    }

    public Set<Range> getRanges() {
        return ranges;
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
