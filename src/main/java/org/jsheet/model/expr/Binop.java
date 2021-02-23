package org.jsheet.model.expr;

import org.jsheet.model.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

import static org.jsheet.model.Type.BOOLEAN;
import static org.jsheet.model.Type.DOUBLE;

public class Binop extends Expr {
    private final String op;
    private final Expr lhs;
    private final Expr rhs;

    public Binop(String op, Expr lhs, Expr rhs) {
        this.op = op;
        this.lhs = lhs;
        this.rhs = rhs;
    }

    @Override
    public Result eval(JSheetTableModel model, Map<String, JSheetCell> refToCell) {
        List<Expr> params = Arrays.asList(lhs, rhs);
        List<Value> values = evaluate(params, model, refToCell);
        if (values == null)
            return evaluationError;
        if (isArithmetic())
            return evalArithmetic(values);
        if (isLogical())
            return evalLogical(values);
        if (isComparison())
            return evalComparison(values);
        throw new AssertionError();
    }

    @SuppressWarnings("Convert2MethodRef")
    private Result evalArithmetic(List<Value> values) {
        List<Type> types = Arrays.asList(DOUBLE, DOUBLE);
        if (!typecheck(values, types))
            return typecheckError;

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

        Value lhsValue = values.get(0);
        Value rhsValue = values.get(1);
        Double result = binary.apply(lhsValue.getAsDouble(), rhsValue.getAsDouble());
        return Result.success(Value.of(result));
    }

    private Result evalLogical(List<Value> values) {
        List<Type> types = Arrays.asList(BOOLEAN, BOOLEAN);
        if (!typecheck(values, types))
            return typecheckError;

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

        Value lhsValue = values.get(0);
        Value rhsValue = values.get(1);
        Boolean result = binary.apply(lhsValue.getAsBoolean(), rhsValue.getAsBoolean());
        return Result.success(Value.of(result));
    }

    @SuppressWarnings("Convert2MethodRef")
    private Result evalComparison(List<Value> values) {
        List<Type> types = Arrays.asList(DOUBLE, DOUBLE);
        if (!typecheck(values, types))
            return typecheckError;

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

        Value lhsValue = values.get(0);
        Value rhsValue = values.get(1);
        Boolean result = binary.apply(lhsValue.getAsDouble(), rhsValue.getAsDouble());
        return Result.success(Value.of(result));
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Binop binop = (Binop) o;

        if (!Objects.equals(op, binop.op)) return false;
        if (!Objects.equals(lhs, binop.lhs)) return false;
        return Objects.equals(rhs, binop.rhs);
    }

    @Override
    public int hashCode() {
        int result = op != null ? op.hashCode() : 0;
        result = 31 * result + (lhs != null ? lhs.hashCode() : 0);
        result = 31 * result + (rhs != null ? rhs.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return String.format("%s %s %s", lhs, op, rhs);
    }
}
