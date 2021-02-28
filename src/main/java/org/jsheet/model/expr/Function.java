package org.jsheet.model.expr;

import org.jsheet.model.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.jsheet.model.Result.failure;
import static org.jsheet.model.Type.DOUBLE;
import static org.jsheet.model.Type.STRING;

public class Function extends Expr {
    private final String name;
    private final List<Expr> args;

    public Function(String name, List<Expr> args) {
        this.name = name;
        this.args = args;
    }

    @Override
    public Result eval(JSheetTableModel model) {
        if (name.equals("pow")) {
            return evalPow(args, model);
        }
        if (name.equals("length")) {
            return evalLength(args, model);
        }
        if (name.equals("sum")) {
            return evalSum(args, model);
        }
        return failure("Unknown function: " + name);
    }

    private Result evalPow(List<Expr> args, JSheetTableModel model) {
        if (args.size() != 2)
            return failure(wrongNumberOfArgsMessage("pow"));

        List<Value> values = evaluate(args, model);
        if (values == null)
            return evaluationError;

        List<Type> types = Arrays.asList(DOUBLE, DOUBLE);
        if (!typecheck(values, types))
            return typecheckError;

        Value baseValue = values.get(0);
        Value expValue = values.get(1);
        double result = Math.pow(baseValue.getAsDouble(), expValue.getAsDouble());
        return Result.success(Value.of(result));
    }

    private Result evalLength(List<Expr> args, JSheetTableModel model) {
        if (args.size() != 1)
            return failure(wrongNumberOfArgsMessage("length"));

        List<Value> values = evaluate(args, model);
        if (values == null)
            return evaluationError;

        List<Type> types = Collections.singletonList(STRING);
        if (!typecheck(values, types))
            return typecheckError;

        Value strValue = values.get(0);
        double result = strValue.getAsString().length();
        return Result.success(Value.of(result));
    }

    private Result evalSum(List<Expr> args, JSheetTableModel model) {
        if (args.size() != 1)
            return failure(wrongNumberOfArgsMessage("sum"));

        Value range = evaluate(args.get(0), model);
        if (range == null)
            return evaluationError;

        if (range.getTag() != Type.RANGE) {
            String msg = typeMismatchMessage(Type.RANGE, range.getTag());
            return Result.failure(msg);
        }

        double sum = 0;
        for (var c : range.getAsRange()) {
            Result res = model.getResultAt(c);
            if (!res.isPresent())
                return res;
            Value addend = res.get();
            if (addend.getTag() != DOUBLE) {
                String msg = typeMismatchMessage(Type.DOUBLE, addend.getTag());
                return Result.failure(msg);
            }
            sum += addend.getAsDouble();
        }
        return Result.success(Value.of(sum));
    }

    private String wrongNumberOfArgsMessage(String name) {
        return "Wrong number of arguments for function: " + name;
    }

    @Override
    public Function shift(JSheetTableModel model, int rowShift, int columnShift) {
        List<Expr> shiftedArgs = args.stream()
            .map(e -> e.shift(model, rowShift, columnShift))
            .collect(Collectors.toList());
        return new Function(name, shiftedArgs);
    }

    @Override
    public Stream<Ref> getRefs() {
        return args.stream().flatMap(Expr::getRefs);
    }

    @Override
    public Stream<Range> getRanges() {
        return args.stream().flatMap(Expr::getRanges);
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
            .map(Expr::toString)
            .collect(Collectors.joining(", "));
        return String.format("%s(%s)", name, argsStr);
    }
}
