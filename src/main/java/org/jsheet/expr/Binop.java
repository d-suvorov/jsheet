package org.jsheet.expr;

import org.jsheet.model.JSheetCell;
import org.jsheet.model.JSheetTableModel;
import org.jsheet.model.Result;

import java.util.Map;
import java.util.function.BiFunction;

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
        Result lhsResult = lhs.eval(model, refToCell);
        Result rhsResult = rhs.eval(model, refToCell);
        BiFunction<Double, Double, Double> binary;
        switch (op) {
            case "+": binary = (a, b) -> a + b; break;
            case "-": binary = (a, b) -> a - b; break;
            case "*": binary = (a, b) -> a * b; break;
            case "/": binary = (a, b) -> a / b; break;
            default: throw new AssertionError();
        }
        return Result.compose(lhsResult, rhsResult, binary);
    }

    @Override
    public String toString() {
        return String.format("%s %s %s", lhs, op, rhs);
    }
}
