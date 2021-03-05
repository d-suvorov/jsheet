package org.jsheet.expression;

import org.jsheet.data.JSheetTableModel;
import org.jsheet.data.Result;
import org.jsheet.data.Value;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.jsheet.data.Type.*;

public class Function extends Expression {
    private final String name;
    private final List<Expression> args;

    public Function(String name, List<Expression> args) {
        this.name = name;
        this.args = args;
    }

    @Override
    public Value eval(JSheetTableModel model) throws EvaluationException {
        if (name.equals("pow")) {
            return evalPow(args, model);
        }
        if (name.equals("length")) {
            return evalLength(args, model);
        }
        if (name.equals("sum")) {
            return evalSum(args, model);
        }
        throw new EvaluationException("Unknown function: " + name);
    }

    private Value evalPow(List<Expression> args, JSheetTableModel model)
        throws EvaluationException
    {
        checkArgumentsNumber("pow", 2, args.size());
        List<Value> values = evaluate(args, model);
        typecheck(values, Arrays.asList(DOUBLE, DOUBLE));
        Value baseValue = values.get(0);
        Value expValue = values.get(1);
        double result = Math.pow(baseValue.getAsDouble(), expValue.getAsDouble());
        return Value.of(result);
    }

    private Value evalLength(List<Expression> args, JSheetTableModel model)
        throws EvaluationException
    {
        checkArgumentsNumber("length", 1, args.size());
        List<Value> values = evaluate(args, model);
        typecheck(values, Collections.singletonList(STRING));
        Value strValue = values.get(0);
        double result = strValue.getAsString().length();
        return Value.of(result);
    }

    private Value evalSum(List<Expression> args, JSheetTableModel model)
        throws EvaluationException
    {
        checkArgumentsNumber("sum", 1, args.size());
        Value range = evaluate(args.get(0), model);
        typecheck(range, RANGE);
        double sum = 0;
        for (var c : range.getAsRange()) {
            Result res = model.getResultAt(c);
            if (!res.isPresent())
                throw new EvaluationException(res.message());
            Value addend = res.get();
            typecheck(addend, DOUBLE);
            sum += addend.getAsDouble();
        }
        return Value.of(sum);
    }

    private void checkArgumentsNumber(String name, int expected, int actual)
        throws EvaluationException
    {
        if (expected != actual) {
            String message = "Wrong number of arguments for function: " + name;
            throw new EvaluationException(message);
        }
    }

    @Override
    public Function shift(JSheetTableModel model, int rowShift, int columnShift) {
        List<Expression> shiftedArgs = args.stream()
            .map(e -> e.shift(model, rowShift, columnShift))
            .collect(Collectors.toList());
        return new Function(name, shiftedArgs);
    }

    @Override
    public Stream<Reference> getReferences() {
        return args.stream().flatMap(Expression::getReferences);
    }

    @Override
    public Stream<Range> getRanges() {
        return args.stream().flatMap(Expression::getRanges);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Function function = (Function) o;

        if (!Objects.equals(name, function.name)) return false;
        return Objects.equals(args, function.args);
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (args != null ? args.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        String argsStr = args.stream()
            .map(Expression::toString)
            .collect(Collectors.joining(", "));
        return String.format("%s(%s)", name, argsStr);
    }
}
