package org.jsheet.model;

import org.jsheet.model.expression.Expression;
import org.jsheet.model.expression.Range;
import org.jsheet.model.expression.Reference;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@SuppressWarnings("ExcessiveLambdaUsage")
public class Formula {
    public final String originalDefinition;

    private final boolean isParsed;
    private final String parsingError;

    public final Expression expression;
    private final List<Reference> references;
    private final List<Range> ranges;

    private Result result;

    /**
     * For successfully parsed expression.
     */
    public Formula(String originalDefinition, Expression expression,
        List<Reference> references, List<Range> ranges)
    {
        this.originalDefinition = originalDefinition;
        this.isParsed = true;
        this.parsingError = null;
        this.expression = expression;
        this.references = references;
        this.ranges = ranges;
    }

    /**
     * For expression parsed with an error.
     */
    public Formula(String originalDefinition, String parsingError) {
        this.originalDefinition = originalDefinition;
        this.isParsed = false;
        this.parsingError = parsingError;
        this.expression = null;
        this.references = null;
        this.ranges = null;
    }

    public boolean isParsed() {
        return isParsed;
    }

    /**
     * @return a list of all references in this formula, including the first
     * and the last references for each range (e.g. A1 and A10 for A1:A10)
     */
    public List<Reference> getReferences() {
        Objects.requireNonNull(references, () -> "getting references of an incorrect formula");
        return Collections.unmodifiableList(references);
    }

    /**
     * @return a list of all ranges in this formula.
     */
    public List<Range> getRanges() {
        Objects.requireNonNull(ranges, () -> "getting ranges of an incorrect formula");
        return Collections.unmodifiableList(ranges);
    }

    /**
     * Evaluates this expression and returns the result.
     * The user must call {@link Formula#resolveReferences(JSheetTableModel)}
     * before calling this method to resolve all references that occur in
     * the current expression.
     */
    public Result eval(JSheetTableModel model) {
        if (!isParsed()) {
            result = Result.failure(parsingError);
        } else {
            Objects.requireNonNull(expression, () -> "evaluating an incorrect formula");
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
    void resolveReferences(JSheetTableModel model) {
        Objects.requireNonNull(references, () -> "getting references of an incorrect formula");
        references.forEach(r -> r.resolve(model));
    }

    public Formula shift(JSheetTableModel model, int rowShift, int columnShift) {
        if (!isParsed) {
            // There's nothing more we can do, treat not parsed formula as a string
            return new Formula(originalDefinition, parsingError);
        }
        Objects.requireNonNull(expression);
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
