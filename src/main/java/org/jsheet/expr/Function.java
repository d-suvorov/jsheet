package org.jsheet.expr;

import java.util.List;
import java.util.stream.Collectors;

public class Function extends Expr {
    private final String name;
    private final List<Expr> args;

    public Function(String name, List<Expr> args) {
        this.name = name;
        this.args = args;
    }

    @Override
    public double eval() {
        if (name.equals("pow")) {
            return evalPow();
        }
        throw new AssertionError("unimplemented");
    }

    private double evalPow() {
        if (args.size() != 2)
            throw new AssertionError();
        double base = args.get(0).eval();
        double exp = args.get(1).eval();
        return Math.pow(base, exp);
    }

    @Override
    public String toString() {
        String argsStr = args.stream()
            .map(Expr::toString)
            .collect(Collectors.joining(", "));
        return String.format("%s(%s)", name, argsStr);
    }
}
