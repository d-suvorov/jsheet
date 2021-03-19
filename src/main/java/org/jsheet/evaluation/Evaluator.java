package org.jsheet.evaluation;

import org.jsheet.data.Cell;
import org.jsheet.data.JSheetTableModel;
import org.jsheet.expression.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import static org.jsheet.evaluation.Type.*;

public class Evaluator implements ExpressionVisitor<Value> {
    private final JSheetTableModel model;

    public Evaluator(JSheetTableModel model) {
        this.model = model;
    }

    @Override
    public Value visit(Binop binop) {
        Value leftValue = binop.getLeft().accept(this);
        Value rightValue = binop.getRight().accept(this);
        String op = binop.getOp();
        if (isArithmetic(op))
            return evalArithmetic(op, leftValue, rightValue);
        if (isLogical(op))
            return evalLogical(op, leftValue, rightValue);
        if (isComparison(op))
            return evalComparison(op, leftValue, rightValue);
        throw new AssertionError();
    }

    @SuppressWarnings("Convert2MethodRef")
    private Value evalArithmetic(String op, Value left, Value right) {
        typecheck(left, DOUBLE);
        typecheck(right, DOUBLE);
        BiFunction<Double, Double, Double> binary;
        switch (op) {
            case "+":
                binary = (a, b) -> a + b;
                break;
            case "-":
                binary = (a, b) -> a - b;
                break;
            case "*":
                binary = (a, b) -> a * b;
                break;
            case "/":
                binary = (a, b) -> a / b;
                break;
            default:
                throw new AssertionError();
        }
        Double result = binary.apply(left.getAsDouble(), right.getAsDouble());
        return Value.of(result);
    }

    private Value evalLogical(String op, Value left, Value right) {
        typecheck(left, BOOLEAN);
        typecheck(right, BOOLEAN);
        BiFunction<Boolean, Boolean, Boolean> binary;
        switch (op) {
            case "&&":
                binary = Boolean::logicalAnd;
                break;
            case "||":
                binary = Boolean::logicalOr;
                break;
            default:
                throw new AssertionError();
        }
        Boolean result = binary.apply(left.getAsBoolean(), right.getAsBoolean());
        return Value.of(result);
    }

    @SuppressWarnings("Convert2MethodRef")
    private Value evalComparison(String op, Value left, Value right) {
        typecheck(left, DOUBLE);
        typecheck(right, DOUBLE);
        BiFunction<Double, Double, Boolean> binary;
        switch (op) {
            case "<":
                binary = (a, b) -> a < b;
                break;
            case "<=":
                binary = (a, b) -> a <= b;
                break;
            case ">":
                binary = (a, b) -> a > b;
                break;
            case ">=":
                binary = (a, b) -> a >= b;
                break;
            case "==":
                binary = (a, b) -> a.equals(b);
                break;
            case "!=":
                binary = (a, b) -> !a.equals(b);
                break;
            default:
                throw new AssertionError();
        }
        Boolean result = binary.apply(left.getAsDouble(), right.getAsDouble());
        return Value.of(result);
    }

    private boolean isArithmetic(String op) {
        return List.of("+", "-", "*", "/").contains(op);
    }

    private boolean isLogical(String op) {
        return List.of("&&", "||").contains(op);
    }

    private boolean isComparison(String op) {
        return List.of("<", "<=", ">", ">=", "==", "!=").contains(op);
    }

    @Override
    public Value visit(Conditional conditional) {
        Value condValue = conditional.getCondition().accept(this);
        typecheck(condValue, Type.BOOLEAN);
        Expression chosen = condValue.getAsBoolean()
            ? conditional.getThenClause()
            : conditional.getElseClause();
        return chosen.accept(this);
    }

    @Override
    public Value visit(Function function) {
        String name = function.getName();
        List<Expression> args = function.getArgs();
        if (name.equals("pow"))
            return evalPow(args);
        if (name.equals("length"))
            return evalLength(args);
        if (name.equals("sum"))
            return evalSum(args);
        throw new EvaluationException("Unknown function: " + name);
    }

    private Value evalPow(List<Expression> args) {
        checkArgumentsNumber("pow", 2, args.size());
        List<Value> values = evalArgs(args);
        typecheck(values, List.of(DOUBLE, DOUBLE));
        Value baseValue = values.get(0);
        Value expValue = values.get(1);
        double result = Math.pow(baseValue.getAsDouble(), expValue.getAsDouble());
        return Value.of(result);
    }

    private Value evalLength(List<Expression> args) {
        checkArgumentsNumber("length", 1, args.size());
        Value strValue = args.get(0).accept(this);
        typecheck(strValue, STRING);
        double result = strValue.getAsString().length();
        return Value.of(result);
    }

    private Value evalSum(List<Expression> args) {
        checkArgumentsNumber("sum", 1, args.size());
        Value range = args.get(0).accept(this);
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

    private void checkArgumentsNumber(String name, int expected, int actual) {
        if (expected != actual) {
            String message = "Wrong number of arguments for function: " + name;
            throw new EvaluationException(message);
        }
    }

    private List<Value> evalArgs(List<Expression> args) {
        List<Value> values = new ArrayList<>(args.size());
        for (var arg : args) {
            values.add(arg.accept(this));
        }
        return values;
    }

    @Override
    public Value visit(BooleanLiteral literal) {
        return Value.of(literal.getValue());
    }

    @Override
    public Value visit(DoubleLiteral literal) {
        return Value.of(literal.getValue());
    }

    @Override
    public Value visit(StringLiteral literal) {
        return Value.of(literal.getValue());
    }

    @Override
    public Value visit(Range range) {
        Reference first = range.getFirst();
        Reference last = range.getLast();
        if (!first.isResolved())
            throw new EvaluationException(unresolvedMessage(first));
        if (!last.isResolved())
            throw new EvaluationException(unresolvedMessage(last));
        Cell firstCell = first.getCell();
        Cell lastCell = last.getCell();
        if (firstCell.getRow() > lastCell.getRow()
            || firstCell.getColumn() > lastCell.getColumn())
        {
            throw new EvaluationException("Incorrect range: " + range);
        }
        RangeValue result = new RangeValue(firstCell, lastCell, range.toString());
        return Value.of(result);
    }

    @Override
    public Value visit(Reference reference) {
        if (!reference.isResolved())
            throw new EvaluationException(unresolvedMessage(reference));
        Result result = model.getResultAt(reference.getCell());
        if (!result.isPresent())
            throw new EvaluationException(result.message());
        return result.get();
    }

    private String unresolvedMessage(Reference reference) {
        return String.format("Reference %s unresolved", reference.getName());
    }

    private void typecheck(List<Value> values, List<Type> types) {
        for (int i = 0; i < values.size(); i++)
            typecheck(values.get(i), types.get(i));
    }

    private void typecheck(Value value, Type type) {
        if (value.getTag() != type) {
            String message = typeMismatchMessage(type, value.getTag());
            throw new EvaluationException(message);
        }
    }

    private String typeMismatchMessage(Type expected, Type actual) {
        return String.format("Expected %s and got %s", expected.name(), actual.name());
    }
}
