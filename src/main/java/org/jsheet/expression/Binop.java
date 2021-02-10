package org.jsheet.expression;

public class Binop extends Expr {
    enum Tag {
        ADD('+'), SUB('-'), MUL('*'), DIV('/');

        final char op;

        Tag(char op) {
            this.op = op;
        }
    }

    private Tag tag;
    private Expr lhs;
    private Expr rhs;

    @Override
    public double eval() {
        double lhsValue = lhs.eval();
        double rhsValue = rhs.eval();
        switch (tag) {
            case ADD: return lhsValue + rhsValue;
            case SUB: return lhsValue - rhsValue;
            case MUL: return lhsValue * rhsValue;
            case DIV: return lhsValue / rhsValue;
        }
        throw new AssertionError();
    }

    @Override
    public String toString() {
        return String.format("%s %s %s", lhs, tag.op, rhs);
    }
}
