package org.jsheet.model.expression;

import org.jsheet.model.JSheetTableModel;
import org.jsheet.model.Type;
import org.jsheet.model.Value;

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
    public Value eval(JSheetTableModel model) {
        Value condValue = evaluate(condition, model);
        if (condValue == null)
            return evaluationError;
        if (!typecheck(condValue, Type.BOOLEAN))
            return typecheckError;
        Expression chosen = condValue.getAsBoolean() ? thenClause : elseClause;
        Value resultValue = evaluate(chosen, model);
        if (resultValue == null)
            return evaluationError;
        return resultValue;
    }

    @Override
    public Expression shift(JSheetTableModel model, int rowShift, int columnShift) {
        return new Conditional(
            condition.shift(model, rowShift, columnShift),
            thenClause.shift(model, rowShift, columnShift),
            elseClause.shift(model, rowShift, columnShift)
        );
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
