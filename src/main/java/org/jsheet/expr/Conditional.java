package org.jsheet.expr;

import org.jsheet.model.JSheetCell;
import org.jsheet.model.JSheetTableModel;
import org.jsheet.model.Result;
import org.jsheet.model.Value;

import java.util.Map;
import java.util.Objects;

public class Conditional extends Expr {
    private final Expr condition;
    private final Expr thenClause;
    private final Expr elseClause;

    public Conditional(Expr condition, Expr ifClause, Expr elseClause) {
        this.condition = condition;
        this.thenClause = ifClause;
        this.elseClause = elseClause;
    }

    @Override
    public Result eval(JSheetTableModel model, Map<String, JSheetCell> refToCell) {
        Value condValue = evaluate(condition, model, refToCell);
        if (condValue == null)
            return evaluationError;
        if (condValue.getTag() != Value.Type.BOOLEAN) {
            String msg = typeMismatchMessage(Value.Type.BOOLEAN, condValue.getTag());
            return Result.failure(msg);
        }
        Expr chosen = condValue.getAsBoolean() ? thenClause : elseClause;
        Value resultValue = evaluate(chosen, model, refToCell);
        if (resultValue == null)
            return evaluationError;
        return Result.success(resultValue);
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
