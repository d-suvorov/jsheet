package org.jsheet.expr;

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
    public double eval() {
        double lhsValue = lhs.eval();
        double rhsValue = rhs.eval();
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
