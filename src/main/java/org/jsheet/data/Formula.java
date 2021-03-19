package org.jsheet.data;

import org.jsheet.expression.*;
import org.jsheet.evaluation.EvaluationException;
import org.jsheet.evaluation.Evaluator;
import org.jsheet.evaluation.Result;
import org.jsheet.evaluation.Value;

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

    public Formula(String originalDefinition, Expression expression) {
        this.originalDefinition = originalDefinition;
        this.expression = expression;
        this.references = expression.getReferences().collect(Collectors.toList());
        this.ranges = expression.getRanges().collect(Collectors.toList());
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
            Value value = expression.evaluate(new Evaluator(model));
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

    /**
     * @return a copy of this formula with cell references
     * shifted by {@code rowShift} and {@code columnShift} respectively.
     */
    public Formula shift(JSheetTableModel model, int rowShift, int columnShift) {
        ExpressionShifter shifter = new ExpressionShifter(model, rowShift, columnShift);
        Expression shiftedExpr = expression.accept(shifter);
        List<Reference> references = shiftedExpr
            .getReferences()
            .collect(Collectors.toList());
        List<Range> ranges = shiftedExpr
            .getRanges()
            .collect(Collectors.toList());
        String newDefinition = "= " + shiftedExpr.toString();
        return new Formula(newDefinition, shiftedExpr);
    }

    private static class ExpressionShifter implements ExpressionVisitor<Expression> {
        final JSheetTableModel model;
        final int rowShift;
        final int columnShift;

        private ExpressionShifter(JSheetTableModel model, int rowShift, int columnShift) {
            this.model = model;
            this.rowShift = rowShift;
            this.columnShift = columnShift;
        }

        @Override
        public Binop visit(Binop binop) {
            return new Binop(
                binop.getOp(),
                binop.getLeft().accept(this),
                binop.getRight().accept(this)
            );
        }

        @Override
        public Conditional visit(Conditional conditional) {
            return new Conditional(
                conditional.getCondition().accept(this),
                conditional.getThenClause().accept(this),
                conditional.getElseClause().accept(this)
            );
        }

        @Override
        public Function visit(Function function) {
            List<Expression> shiftedArgs = function.getArgs().stream()
                .map(arg -> arg.accept(this))
                .collect(Collectors.toList());
            return new Function(function.getName(), shiftedArgs);
        }

        @Override
        public BooleanLiteral visit(BooleanLiteral literal) {
            return literal; // Plain values are immutable
        }

        @Override
        public DoubleLiteral visit(DoubleLiteral literal) {
            return literal; // Plain values are immutable
        }

        @Override
        public StringLiteral visit(StringLiteral literal) {
            return literal; // Plain values are immutable
        }

        @Override
        public Range visit(Range range) {
            return new Range(
                visit(range.getFirst()),
                visit(range.getLast())
            );
        }

        @Override
        public Reference visit(Reference reference) {
            if (!reference.isResolved()) {
                // Leave unresolved references as-is. They only hold
                // a string value, thus it's perfectly fine to re-use them
                return reference;
            }
            Cell cell = reference.getCell();
            boolean rowAbsolute = reference.isRowAbsolute();
            boolean columnAbsolute = reference.isColumnAbsolute();
            int shiftedRow = rowAbsolute ? cell.row : cell.row + rowShift;
            int shiftedColumn = columnAbsolute ? cell.column : cell.column + columnShift;
            if (!model.containsCell(shiftedRow, shiftedColumn))
                return new Reference(Reference.OUT_OF_BOUNDS_REFERENCE_NAME, null, false, false);
            Cell shiftedCell = new Cell(shiftedRow, shiftedColumn);
            String shiftedName = (columnAbsolute ? "$" : "")
                + model.getColumnName(shiftedCell.column)
                + (rowAbsolute ? "$" : "")
                + shiftedCell.row;
            return new Reference(shiftedName, shiftedCell, rowAbsolute, columnAbsolute);
        }
    }

    @Override
    public String toString() {
        return originalDefinition;
    }
}
