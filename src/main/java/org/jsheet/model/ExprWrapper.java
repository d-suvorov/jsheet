package org.jsheet.model;

import org.jsheet.model.expr.Expr;
import org.jsheet.model.expr.Range;
import org.jsheet.model.expr.Ref;

import java.util.Collections;
import java.util.List;

public class ExprWrapper {
    public final String originalDefinition;
    public final Expr expression;

    private Result result;

    private final List<Ref> refs;
    private final List<Range> ranges;

    public ExprWrapper(String originalDefinition, Expr expression,
        List<Ref> refs, List<Range> ranges)
    {
        this.originalDefinition = originalDefinition;
        this.expression = expression;
        this.refs = refs;
        this.ranges = ranges;
    }

    /**
     * @return a list of all references in this formula, including the first
     * and the last references for each range (e.g. A1 and A10 for A1:A10)
     */
    public List<Ref> getRefs() {
        return Collections.unmodifiableList(refs);
    }

    /**
     * @return a list of all ranges in this formula.
     */
    public List<Range> getRanges() {
        return Collections.unmodifiableList(ranges);
    }

    /**
     * Evaluates this expression and returns the result.
     * The user must call {@link ExprWrapper#resolveRefs(JSheetTableModel)}
     * before calling this method to resolve all references that occur in
     * the current expression.
     */
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

    /**
     * Tries to resolve all references that occur in the current expression.
     */
    void resolveRefs(JSheetTableModel model) {
        refs.forEach(r -> r.resolve(model));
    }

    @Override
    public String toString() {
        return originalDefinition;
    }
}
