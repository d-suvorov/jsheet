package org.jsheet.model;

import org.jsheet.model.expr.Expr;
import org.jsheet.model.expr.Range;
import org.jsheet.model.expr.Ref;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("ExcessiveLambdaUsage")
public class ExprWrapper {
    public final String originalDefinition;

    private final boolean isParsed;
    private final String parsingError;

    public final Expr expression;
    private final List<Ref> refs;
    private final List<Range> ranges;

    private Result result;

    /**
     * Creates a wrapper for successfully parsed formula.
     */
    public ExprWrapper(String originalDefinition, Expr expression,
        List<Ref> refs, List<Range> ranges)
    {
        this.originalDefinition = originalDefinition;
        this.isParsed = true;
        this.parsingError = null;
        this.expression = expression;
        this.refs = refs;
        this.ranges = ranges;
    }

    /**
     * Creates a wrapper for formula parsed with an error.
     */
    public ExprWrapper(String originalDefinition, String parsingError) {
        this.originalDefinition = originalDefinition;
        this.isParsed = false;
        this.parsingError = parsingError;
        this.expression = null;
        this.refs = null;
        this.ranges = null;
    }

    public boolean isParsed() {
        return isParsed;
    }

    /**
     * @return a list of all references in this formula, including the first
     * and the last references for each range (e.g. A1 and A10 for A1:A10)
     */
    public List<Ref> getRefs() {
        Objects.requireNonNull(refs, () -> "getting refs of a not parsed formula");
        return Collections.unmodifiableList(refs);
    }

    /**
     * @return a list of all ranges in this formula.
     */
    public List<Range> getRanges() {
        Objects.requireNonNull(ranges, () -> "getting ranges of a not parsed formula");
        return Collections.unmodifiableList(ranges);
    }

    /**
     * Evaluates this expression and returns the result.
     * The user must call {@link ExprWrapper#resolveRefs(JSheetTableModel)}
     * before calling this method to resolve all references that occur in
     * the current expression.
     */
    public Result eval(JSheetTableModel model) {
        if (!isParsed) {
            result = Result.failure(parsingError);
        } else {
            Objects.requireNonNull(expression, () -> "evaluating a not parsed formula");
            result = expression.eval(model);
        }
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
        Objects.requireNonNull(refs, () -> "getting refs of a not parsed formula");
        refs.forEach(r -> r.resolve(model));
    }

    @Override
    public String toString() {
        return originalDefinition;
    }
}
