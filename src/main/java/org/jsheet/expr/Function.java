package org.jsheet.expr;

import org.jsheet.model.JSheetCell;
import org.jsheet.model.JSheetTableModel;
import org.jsheet.model.Result;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.jsheet.model.Result.failure;
import static org.jsheet.model.Result.success;

public class Function extends Expr {
    private final String name;
    private final List<Expr> args;

    public Function(String name, List<Expr> args) {
        this.name = name;
        this.args = args;
    }

    @Override
    public Result eval(JSheetTableModel model, Map<String, JSheetCell> refToCell) {
        List<Double> argsResults = new ArrayList<>();
        for (var e : args) {
            Result res = e.eval(model, refToCell);
            if (!res.isPresent())
                return res;
            argsResults.add(res.get());
        }
        if (name.equals("pow")) {
            if (argsResults.size() != 2)
                return failure("Wrong number of arguments for function: pow");
            else
                return success(evalPow(argsResults));
        }
        return failure("Unknown function: " + name);
    }

    private double evalPow(List<Double> args) {
        double base = args.get(0);
        double exp = args.get(1);
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
