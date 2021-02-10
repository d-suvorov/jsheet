package org.jsheet.expression;

import java.util.Arrays;
import java.util.stream.Collectors;

public class Function extends Expr {
    private String name;
    private Expr[] args;

    @Override
    public double eval() {
        if (name.equals("pow")) {
            return evalPow();
        }
        throw new AssertionError("unimplemented");
    }

    private double evalPow() {
        if (args.length != 2)
            throw new AssertionError();
        double base = args[0].eval();
        double exp = args[1].eval();
        return Math.pow(base, exp);
    }

    @Override
    public String toString() {
        String argsStr = Arrays.stream(args)
            .map(Expr::toString)
            .collect(Collectors.joining(", "));
        return String.format("%s(%s)", name, argsStr);
    }
}
