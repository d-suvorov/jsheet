package org.jsheet.expression;

import org.jsheet.data.JSheetTableModel;
import org.jsheet.expression.evaluation.Type;
import org.jsheet.expression.evaluation.Value;

import java.util.Objects;
import java.util.stream.Stream;

public class Conditional extends Expression {
    private final Expression condition;
    private final Expression thenClause;
    private final Expression elseClause;

    public Conditional(Expression condition, Expression thenClause, Expression elseClause) {
        this.condition = condition;
        this.thenClause = thenClause;
        this.elseClause = elseClause;
    }

    @Override
    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public Value eval(JSheetTableModel model) throws EvaluationException {
        Value condValue = condition.eval(model);
        typecheck(condValue, Type.BOOLEAN);
        Expression chosen = condValue.getAsBoolean() ? thenClause : elseClause;
        return chosen.eval(model);
    }

    @Override
    public Stream<Reference> getReferences() {
        return Stream.of(condition, thenClause, elseClause)
            .flatMap(Expression::getReferences);
    }

    @Override
    public Stream<Range> getRanges() {
        return Stream.of(condition, thenClause, elseClause)
            .flatMap(Expression::getRanges);
    }

    public Expression getCondition() {
        return condition;
    }

    public Expression getThenClause() {
        return thenClause;
    }

    public Expression getElseClause() {
        return elseClause;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Conditional that = (Conditional) o;

        if (!Objects.equals(condition, that.condition)) return false;
        if (!Objects.equals(thenClause, that.thenClause)) return false;
        return Objects.equals(elseClause, that.elseClause);
    }

    @Override
    public int hashCode() {
        int result = condition != null ? condition.hashCode() : 0;
        result = 31 * result + (thenClause != null ? thenClause.hashCode() : 0);
        result = 31 * result + (elseClause != null ? elseClause.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return String.format("if %s then %s else %s",
            condition, thenClause, elseClause);
    }
}
