package org.jsheet.expr;

import org.jsheet.model.JSheetCell;
import org.jsheet.model.JSheetTableModel;
import org.jsheet.model.Result;
import org.jsheet.model.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.jsheet.model.Result.failure;
import static org.jsheet.model.Value.Type.DOUBLE;

public class Function extends Expr {
    private final String name;
    private final List<Expr> args;

    public Function(String name, List<Expr> args) {
        this.name = name;
        this.args = args;
    }

    @Override
    public Result eval(JSheetTableModel model, Map<String, JSheetCell> refToCell) {
        List<Result> argsResults = new ArrayList<>();
        for (var e : args) {
            Result res = e.eval(model, refToCell);
            if (!res.isPresent())
                return res;
            argsResults.add(res);
        }

        if (name.equals("pow")) {
            return evalPow(argsResults);
        }
        return failure("Unknown function: " + name);
    }

    private Result evalPow(List<Result> args) {
        if (args.size() != 2)
            return failure("Wrong number of arguments for function: pow");

        // TODO refactor and generalize this mess
        Result baseResult = args.get(0);
        Value baseValue = baseResult.get();
        if (baseValue.getTag() != DOUBLE) {
            String msg = String.format(
                "Expected %s and got %s",
                DOUBLE.name(), baseValue.getTag().name());
            return Result.failure(msg);
        }

        Result expResult = args.get(1);
        Value expValue = expResult.get();
        if (expValue.getTag() != DOUBLE) {
            String msg = String.format(
                "Expected %s and got %s",
                DOUBLE.name(), expValue.getTag().name());
            return Result.failure(msg);
        }

        double result = Math.pow(baseValue.getAsDouble(), expValue.getAsDouble());
        return Result.success(Value.of(result));
    }

    @Override
    public String toString() {
        String argsStr = args.stream()
            .map(Expr::toString)
            .collect(Collectors.joining(", "));
        return String.format("%s(%s)", name, argsStr);
    }
}
