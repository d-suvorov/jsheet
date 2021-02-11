package org.jsheet.expr;

import org.jsheet.model.JSheetCell;
import org.jsheet.model.JSheetTableModel;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Function extends Expr {
    private final String name;
    private final List<Expr> args;

    public Function(String name, List<Expr> args) {
        this.name = name;
        this.args = args;
    }

    @Override
    public double eval(JSheetTableModel model, Map<String, JSheetCell> refToCell) {
        if (name.equals("pow")) {
            return evalPow(model, refToCell);
        }
        throw new AssertionError("unimplemented");
    }

    private double evalPow(JSheetTableModel model, Map<String, JSheetCell> refToCell) {
        if (args.size() != 2)
            throw new AssertionError();
        double base = args.get(0).eval(model, refToCell);
        double exp = args.get(1).eval(model, refToCell);
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
