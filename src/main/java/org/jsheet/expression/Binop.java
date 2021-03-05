package org.jsheet.expression;

import org.jsheet.data.JSheetTableModel;
import org.jsheet.data.Type;
import org.jsheet.data.Value;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import static org.jsheet.data.Type.BOOLEAN;
import static org.jsheet.data.Type.DOUBLE;

public class Binop extends Expression {
    private final String op;
    private final Expression left;
    private final Expression right;

    public Binop(String op, Expression left, Expression right) {
        this.op = op;
        this.left = left;
        this.right = right;
    }

    @Override
    public Value eval(JSheetTableModel model) throws EvaluationException {
        List<Expression> params = Arrays.asList(left, right);
        List<Value> values = evaluate(params, model);
        if (isArithmetic())
            return evalArithmetic(values);
        if (isLogical())
            return evalLogical(values);
        if (isComparison())
            return evalComparison(values);
        throw new AssertionError();
    }

    @SuppressWarnings("Convert2MethodRef")
    private Value evalArithmetic(List<Value> values) throws EvaluationException {
        List<Type> types = Arrays.asList(DOUBLE, DOUBLE);
        typecheck(values, types);

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

        Value leftValue = values.get(0);
        Value rightValue = values.get(1);
        Double result = binary.apply(leftValue.getAsDouble(), rightValue.getAsDouble());
        return Value.of(result);
    }

    private Value evalLogical(List<Value> values) throws EvaluationException {
        List<Type> types = Arrays.asList(BOOLEAN, BOOLEAN);
        typecheck(values, types);

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

        Value leftValue = values.get(0);
        Value rightValue = values.get(1);
        Boolean result = binary.apply(leftValue.getAsBoolean(), rightValue.getAsBoolean());
        return Value.of(result);
    }

    @SuppressWarnings("Convert2MethodRef")
    private Value evalComparison(List<Value> values) throws EvaluationException {
        List<Type> types = Arrays.asList(DOUBLE, DOUBLE);
        typecheck(values, types);

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

        Value leftValue = values.get(0);
        Value rightValue = values.get(1);
        Boolean result = binary.apply(leftValue.getAsDouble(), rightValue.getAsDouble());
        return Value.of(result);
    }

    private boolean isArithmetic() {
        return List.of("+", "-", "*", "/").contains(op);
    }

    private boolean isLogical() {
        return List.of("&&", "||").contains(op);
    }

    private boolean isComparison() {
        return List.of("<", "<=", ">", ">=", "==", "!=").contains(op);
    }

    @Override
    public Expression shift(JSheetTableModel model, int rowShift, int columnShift) {
        return new Binop(
            op,
            left.shift(model, rowShift, columnShift),
            right.shift(model, rowShift, columnShift)
        );
    }

    @Override
    public Stream<Reference> getReferences() {
        return Stream.concat(left.getReferences(), right.getReferences());
    }

    @Override
    public Stream<Range> getRanges() {
        return Stream.concat(left.getRanges(), right.getRanges());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Binop binop = (Binop) o;

        if (!Objects.equals(op, binop.op)) return false;
        if (!Objects.equals(left, binop.left)) return false;
        return Objects.equals(right, binop.right);
    }

    @Override
    public int hashCode() {
        int result = op != null ? op.hashCode() : 0;
        result = 31 * result + (left != null ? left.hashCode() : 0);
        result = 31 * result + (right != null ? right.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return String.format("(%s %s %s)", left, op, right);
    }
}
