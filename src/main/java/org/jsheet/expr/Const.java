package org.jsheet.expr;

public class Const extends Expr {
    private final double value;

    public Const(double value) {
        this.value = value;
    }

    @Override
    public double eval() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
