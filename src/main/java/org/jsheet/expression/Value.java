package org.jsheet.expression;

public class Value extends Expr {
    private double value;

    @Override
    public double eval() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
