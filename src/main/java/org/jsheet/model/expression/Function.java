package org.jsheet.model.expression;

import org.jsheet.model.JSheetTableModel;
import org.jsheet.model.Type;
import org.jsheet.model.Value;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.jsheet.model.Type.*;

public class Function extends Expression {
    private final String name;
    private final List<Expression> args;

    public Function(String name, List<Expression> args) {
        this.name = name;
        this.args = args;
    }

    @Override
    public Value eval(JSheetTableModel model) {
        if (name.equals("pow")) {
            return evalPow(args, model);
        }
        if (name.equals("length")) {
            return evalLength(args, model);
        }
        if (name.equals("sum")) {
            return evalSum(args, model);
        }
        return Value.error("Unknown function: " + name);
    }

    private Value evalPow(List<Expression> args, JSheetTableModel model) {
        if (args.size() != 2)
            return Value.error(wrongNumberOfArgsMessage("pow"));

        List<Value> values = evaluate(args, model);
        if (values == null)
            return evaluationError;

        List<Type> types = Arrays.asList(DOUBLE, DOUBLE);
        if (!typecheck(values, types))
            return typecheckError;

        Value baseValue = values.get(0);
        Value expValue = values.get(1);
        double result = Math.pow(baseValue.getAsDouble(), expValue.getAsDouble());
        return Value.of(result);
    }

    private Value evalLength(List<Expression> args, JSheetTableModel model) {
        if (args.size() != 1)
            return Value.error(wrongNumberOfArgsMessage("length"));

        List<Value> values = evaluate(args, model);
        if (values == null)
            return evaluationError;

        List<Type> types = Collections.singletonList(STRING);
        if (!typecheck(values, types))
            return typecheckError;

        Value strValue = values.get(0);
        double result = strValue.getAsString().length();
        return Value.of(result);
    }

    private Value evalSum(List<Expression> args, JSheetTableModel model) {
        if (args.size() != 1)
            return Value.error(wrongNumberOfArgsMessage("sum"));
        Value range = evaluate(args.get(0), model);
        if (range == null)
            return evaluationError;
        if (!typecheck(range, RANGE))
            return typecheckError;

        double sum = 0;
        for (var c : range.getAsRange()) {
            Value addend = model.getResultAt(c);
            if (!addend.isPresent())
                return addend;
            if (!typecheck(addend, DOUBLE))
                return typecheckError;
            sum += addend.getAsDouble();
        }
        return Value.of(sum);
    }

    private String wrongNumberOfArgsMessage(String name) {
        return "Wrong number of arguments for function: " + name;
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
