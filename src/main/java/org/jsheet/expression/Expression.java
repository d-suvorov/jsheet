package org.jsheet.expression;

import org.jsheet.data.JSheetTableModel;
import org.jsheet.data.Type;
import org.jsheet.data.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public abstract class Expression {
    public abstract Value eval(JSheetTableModel model) throws EvaluationException;

    /**
     * @return a copy of this expression with cell references
     * shifted by {@code rowShift} and {@code columnShift} respectively.
     */
    public abstract Expression shift(JSheetTableModel model, int rowShift, int columnShift);

    public abstract Stream<Reference> getReferences();

    public abstract Stream<Range> getRanges();

    /**
     * Evaluates a list of expressions.
     *
     * @return a list of values
     * @throws EvaluationException if an error occurs.
     */
    protected List<Value> evaluate(List<Expression> params, JSheetTableModel model)
        throws EvaluationException
    {
        List<Value> values = new ArrayList<>(params.size());
        for (var p : params) {
            values.add(evaluate(p, model));
        }
        return values;
    }

    /**
     * Same as {@link Expression#evaluate(List, JSheetTableModel) but for a single value}.
     */
    protected Value evaluate(Expression param, JSheetTableModel model)
        throws EvaluationException
    {
        return param.eval(model);
    }

    /**
     * Typechecks a list of values.
     *
     * @throws EvaluationException if types mismatch.
     */
    protected void typecheck(List<Value> values, List<Type> types) throws EvaluationException {
        for (int i = 0; i < values.size(); i++)
            typecheck(values.get(i), types.get(i));
    }

    protected void typecheck(Value value, Type type) throws EvaluationException {
        if (value.getTag() != type) {
            String message = typeMismatchMessage(type, value.getTag());
            throw new EvaluationException(message);
        }
    }

    private String typeMismatchMessage(Type expected, Type actual) {
        return String.format("Expected %s and got %s", expected.name(), actual.name());
    }
}
