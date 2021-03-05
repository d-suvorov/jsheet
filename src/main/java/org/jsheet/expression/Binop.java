package org.jsheet.expression;

import org.jsheet.data.JSheetTableModel;
import org.jsheet.data.Value;

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
        Value leftValue = evaluate(left, model);
        Value rightValue = evaluate(right, model);
        if (isArithmetic())
            return evalArithmetic(leftValue, rightValue);
        if (isLogical())
            return evalLogical(leftValue, rightValue);
        if (isComparison())
            return evalComparison(leftValue, rightValue);
        throw new AssertionError();
    }

    @SuppressWarnings("Convert2MethodRef")
    private Value evalArithmetic(Value leftValue, Value rightValue) throws EvaluationException {
        typecheck(leftValue, DOUBLE);
        typecheck(rightValue, DOUBLE);
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
        Double result = binary.apply(leftValue.getAsDouble(), rightValue.getAsDouble());
        return Value.of(result);
    }

    private Value evalLogical(Value leftValue, Value rightValue) throws EvaluationException {
        typecheck(leftValue, BOOLEAN);
        typecheck(rightValue, BOOLEAN);
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
        Boolean result = binary.apply(leftValue.getAsBoolean(), rightValue.getAsBoolean());
        return Value.of(result);
    }

    @SuppressWarnings("Convert2MethodRef")
    private Value evalComparison(Value leftValue, Value rightValue) throws EvaluationException {
        typecheck(leftValue, DOUBLE);
        typecheck(rightValue, DOUBLE);
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
