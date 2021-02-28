package org.jsheet.model.expression;

import org.jsheet.model.JSheetTableModel;
import org.jsheet.model.Result;
import org.jsheet.model.Type;
import org.jsheet.model.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public abstract class Expression {
    protected Result evaluationError;
    protected Result typecheckError;

    public abstract Result eval(JSheetTableModel model);

    /**
     * @return a copy of this expression with cell references
     * shifted by {@code rowShift} and {@code columnShift} respectively.
     */
    public abstract Expression shift(JSheetTableModel model, int rowShift, int columnShift);

    public abstract Stream<Reference> getReferences();

    public abstract Stream<Range> getRanges();

    /**
     * Evaluates a list of expressions.
     * Writes {@code evaluationError} if an error occurs.
     *
     * @return a list of values or {@code null} if an error occurs.
     */
    protected List<Value> evaluate(List<Expression> params, JSheetTableModel model) {
        List<Value> values = new ArrayList<>(params.size());
        for (var p : params) {
            Value value = evaluate(p, model);
            if (value == null)
                return null;
            values.add(value);
        }
        return values;
    }

    /**
     * Same as {@link Expression#evaluate(List, JSheetTableModel) but for a single value}.
     */
    protected Value evaluate(Expression param, JSheetTableModel model) {
        Result res = param.eval(model);
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
    protected boolean typecheck(List<Value> values, List<Type> types) {
        for (int i = 0; i < values.size(); i++) {
            if (!typecheck(values.get(i), types.get(i)))
                return false;
        }
        return true;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    protected boolean typecheck(Value value, Type type) {
        if (value.getTag() == type)
            return true;
        String message = typeMismatchMessage(type, value.getTag());
        typecheckError = Result.failure(message);
        return false;
    }

    protected String typeMismatchMessage(Type expected, Type actual) {
        return String.format("Expected %s and got %s", expected.name(), actual.name());
    }
}
