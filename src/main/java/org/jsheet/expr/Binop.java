package org.jsheet.expr;

import org.jsheet.model.JSheetCell;
import org.jsheet.model.JSheetTableModel;
import org.jsheet.model.Result;
import org.jsheet.model.Value;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

import static org.jsheet.model.Value.Type.DOUBLE;

public class Binop extends Expr {
    private final String op;
    private final Expr lhs;
    private final Expr rhs;

    public Binop(String op, Expr lhs, Expr rhs) {
        this.op = op;
        this.lhs = lhs;
        this.rhs = rhs;
    }

    @SuppressWarnings("Convert2MethodRef")
    @Override
    public Result eval(JSheetTableModel model, Map<String, JSheetCell> refToCell) {
        List<Expr> params = Arrays.asList(lhs, rhs);
        List<Value> values = evaluate(params, model, refToCell);
        if (values == null)
            return evaluationError;

        List<Value.Type> types = Arrays.asList(DOUBLE, DOUBLE);
        if (!typecheck(values, types))
            return typecheckError;

        BiFunction<Double, Double, Double> binary;
        switch (op) {
            case "+": binary = (a, b) -> a + b; break;
            case "-": binary = (a, b) -> a - b; break;
            case "*": binary = (a, b) -> a * b; break;
            case "/": binary = (a, b) -> a / b; break;
            default: throw new AssertionError();
        }

        Value lhsValue = values.get(0);
        Value rhsValue = values.get(1);
        Double result = binary.apply(lhsValue.getAsDouble(), rhsValue.getAsDouble());
        return Result.success(Value.of(result));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Binop binop = (Binop) o;

        if (!Objects.equals(op, binop.op)) return false;
        if (!Objects.equals(lhs, binop.lhs)) return false;
        return Objects.equals(rhs, binop.rhs);
    }

    @Override
    public int hashCode() {
        int result = op != null ? op.hashCode() : 0;
        result = 31 * result + (lhs != null ? lhs.hashCode() : 0);
        result = 31 * result + (rhs != null ? rhs.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return String.format("%s %s %s", lhs, op, rhs);
    }
}
