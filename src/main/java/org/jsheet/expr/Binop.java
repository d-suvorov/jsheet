package org.jsheet.expr;

import org.jsheet.model.JSheetCell;
import org.jsheet.model.JSheetTableModel;

import java.util.Map;

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
    public double eval(JSheetTableModel model, Map<String, JSheetCell> refToCell) {
        double lhsValue = lhs.eval(model, refToCell);
        double rhsValue = rhs.eval(model, refToCell);
        switch (op) {
            case "+": return lhsValue + rhsValue;
            case "-": return lhsValue - rhsValue;
            case "*": return lhsValue * rhsValue;
            case "/": return lhsValue / rhsValue;
        }
        throw new AssertionError();
    }

    @Override
    public String toString() {
        return String.format("%s %s %s", lhs, op, rhs);
    }
}
