package org.jsheet.data;

import org.jsheet.expression.EvaluationException;
import org.jsheet.expression.Expression;
import org.jsheet.expression.Range;
import org.jsheet.expression.Reference;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@SuppressWarnings("ExcessiveLambdaUsage")
public class Formula {
    public final String originalDefinition;

    private final Expression expression;
    private final List<Reference> references;
    private final List<Range> ranges;

    private Result result;

    public Formula(String originalDefinition, Expression expression,
        List<Reference> references, List<Range> ranges)
    {
        this.originalDefinition = originalDefinition;
        this.expression = expression;
        this.references = references;
        this.ranges = ranges;
    }

    /**
     * @return a list of all references in this formula, including the first
     * and the last references for each range (e.g. A1 and A10 for A1:A10)
     */
    public List<Reference> getReferences() {
        return Collections.unmodifiableList(references);
    }

    /**
     * @return a list of all ranges in this formula.
     */
    public List<Range> getRanges() {
        return Collections.unmodifiableList(ranges);
    }

    /**
     * Evaluates this expression and stores the result, which can later
     * be retrieved with {@link Formula#getResult()} method.
     * The user must call {@link Formula#resolveReferences(JSheetTableModel)}
     * before calling this method to resolve all references that occur in
     * the current expression.
     */
    public void eval(JSheetTableModel model) {
        try {
            Value value = expression.eval(model);
            result = Result.success(value);
        } catch (EvaluationException e) {
            result = Result.failure(e.getMessage());
        }
    }

    /**
     * @return previously computed formula value.
     */
    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    /**
     * Tries to resolve all references that occur in the current expression.
     */
    void resolveReferences(JSheetTableModel model) {
        Objects.requireNonNull(references, () -> "getting references of an incorrect formula");
        references.forEach(r -> r.resolve(model));
    }

    public Formula shift(JSheetTableModel model, int rowShift, int columnShift) {
        Expression shiftedExpr = expression.shift(model, rowShift, columnShift);
        List<Reference> references = shiftedExpr
            .getReferences()
            .collect(Collectors.toList());
        List<Range> ranges = shiftedExpr
            .getRanges()
            .collect(Collectors.toList());
        String newDefinition = "= " + shiftedExpr.toString();
        return new Formula(newDefinition, shiftedExpr, references, ranges);
    }

    @Override
    public String toString() {
        return originalDefinition;
    }
}
