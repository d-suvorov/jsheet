package org.jsheet.expression;

public class Ref extends Expr {
    private String name;

    @Override
    public double eval() {
        throw new AssertionError("unimplemented");
    }

    @Override
    public String toString() {
        return name;
    }
}
