package org.jsheet.expr;

public class Ref extends Expr {
    private final String name;

    public Ref(String name) {
        this.name = name;
    }

    @Override
    public double eval() {
        throw new AssertionError("unimplemented");
    }

    @Override
    public String toString() {
        return name;
    }
}
