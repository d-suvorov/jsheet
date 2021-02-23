package org.jsheet.expr;

import org.jsheet.model.JSheetCell;
import org.jsheet.model.JSheetTableModel;
import org.jsheet.model.Result;
import org.jsheet.model.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class Expr {
    public abstract Result eval(JSheetTableModel model, Map<String, JSheetCell> refToCell);

    protected Result evaluationError;
    protected Result typecheckError;

    /**
     * Evaluates a list of expressions.
     * Writes {@code evaluationError} if an error occurs.
     *
     * @return a list of values or {@code null} if an error occurs.
     */
    protected List<Value> evaluate(List<Expr> params,
        JSheetTableModel model, Map<String, JSheetCell> refToCell)
    {
        List<Value> values = new ArrayList<>(params.size());
        for (var p : params) {
            Result res = p.eval(model, refToCell);
            if (!res.isPresent()) {
                evaluationError = res;
                return null;
            }
            values.add(res.get());
        }
        return values;
    }

    /**
     * Same as {@link Expr#evaluate(List, JSheetTableModel, Map) but for a single value}.
     */
    protected Value evaluate(Expr param,
        JSheetTableModel model, Map<String, JSheetCell> refToCell)
    {
        Result res = param.eval(model, refToCell);
        if (!res.isPresent()) {
            evaluationError = res;
            return null;
        }
        return res.get();
    }

    /**
     * Typechecks a list of values.
     * Writes {@code typecheckError} if types mismatch.
     *
     * @return {@code true} iff all the values have correct type.
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    protected boolean typecheck(List<Value> values, List<Value.Type> types) {
        for (int i = 0; i < values.size(); i++) {
            Value.Type expected = types.get(i);
            Value.Type actual = values.get(i).getTag();
            if (actual != expected) {
                String msg = typeMismatchMessage(expected, actual);
                typecheckError = Result.failure(msg);
                return false;
            }
        }
        return true;
    }

    protected String typeMismatchMessage(Value.Type expected, Value.Type actual) {
        return String.format("Expected %s and got %s", expected.name(), actual.name());
    }
}
