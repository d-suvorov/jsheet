package org.jsheet.expr;

import org.jsheet.model.JSheetCell;
import org.jsheet.model.JSheetTableModel;
import org.jsheet.model.Result;
import org.jsheet.model.Value;

import java.util.Map;
import java.util.function.BiFunction;

import static org.jsheet.model.Value.Type.DOUBLE;

public class Binop extends Expr {
    private final String op;
    private final Expr lhs;
    private final Expr rhs;

    public Binop(String op, Expr lhs, Expr rhs) {
        this.op = op;
        this.lhs = lhs;
        this.rhs = rhs;
    }

    @SuppressWarnings("Convert2MethodRef")
    @Override
    public Result eval(JSheetTableModel model, Map<String, JSheetCell> refToCell) {
        // TODO refactor and generalize this mess
        Result lhsResult = lhs.eval(model, refToCell);
        if (!lhsResult.isPresent())
            return lhsResult;
        Value lhsValue = lhsResult.get();
        if (lhsValue.getTag() != DOUBLE) {
            String msg = String.format(
                "Expected %s and got %s",
                DOUBLE.name(), lhsValue.getTag().name());
            return Result.failure(msg);
        }

        Result rhsResult = rhs.eval(model, refToCell);
        if (!rhsResult.isPresent())
            return rhsResult;
        Value rhsValue = rhsResult.get();
        if (lhsValue.getTag() != DOUBLE) {
            String msg = String.format(
                "Expected %s and got %s",
                DOUBLE.name(), lhsValue.getTag().name());
            return Result.failure(msg);
        }

        BiFunction<Double, Double, Double> binary;
        switch (op) {
            case "+": binary = (a, b) -> a + b; break;
            case "-": binary = (a, b) -> a - b; break;
            case "*": binary = (a, b) -> a * b; break;
            case "/": binary = (a, b) -> a / b; break;
            default: throw new AssertionError();
        }
        Double result = binary.apply(lhsValue.getAsDouble(), rhsValue.getAsDouble());
        return Result.success(Value.of(result));
    }

    @Override
    public String toString() {
        return String.format("%s %s %s", lhs, op, rhs);
    }
}
