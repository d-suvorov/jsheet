package org.jsheet.evaluation;

import org.jsheet.data.Cell;
import org.jsheet.data.JSheetTableModel;
import org.jsheet.expression.*;

import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.StreamSupport;

import static org.jsheet.evaluation.Type.*;

public class Evaluator implements ExpressionVisitor<Result> {
    private final JSheetTableModel model;

    public Evaluator(JSheetTableModel model) {
        this.model = model;
    }

    @Override
    public Result visit(Binop binop) {
        Result left = binop.getLeft().accept(this);
        Result right = binop.getRight().accept(this);
        String op = binop.getOp();
        if (isArithmetic(op))
            return evalArithmetic(op, left, right);
        if (isLogical(op))
            return evalLogical(op, left, right);
        if (isComparison(op))
            return evalComparison(op, left, right);
        throw new AssertionError();
    }

    @SuppressWarnings("Convert2MethodRef")
    private Result evalArithmetic(String op, Result left, Result right) {
        BiFunction<Double, Double, Double> binary;
        switch (op) {
            case "+":
                binary = (a, b) -> a + b;
                break;
            case "-":
                binary = (a, b) -> a - b;
                break;
            case "*":
                binary = (a, b) -> a * b;
                break;
            case "/":
                binary = (a, b) -> a / b;
                break;
            default:
                throw new AssertionError();
        }
        return left
            .typecheck(DOUBLE)
            .flatMap(l ->
                right
                    .typecheck(DOUBLE)
                    .flatMap(r -> Result.success(
                        Value.of(binary.apply(
                            l.getAsDouble(),
                            r.getAsDouble()
                        ))
                    ))
            );
    }

    private Result evalLogical(String op, Result left, Result right) {
        BiFunction<Boolean, Boolean, Boolean> binary;
        switch (op) {
            case "&&":
                binary = Boolean::logicalAnd;
                break;
            case "||":
                binary = Boolean::logicalOr;
                break;
            default:
                throw new AssertionError();
        }
        return left
            .typecheck(BOOLEAN)
            .flatMap(l ->
                right
                    .typecheck(BOOLEAN)
                    .flatMap(r -> Result.success(
                        Value.of(binary.apply(
                            l.getAsBoolean(),
                            r.getAsBoolean()
                        ))
                    ))
            );
    }

    @SuppressWarnings("Convert2MethodRef")
    private Result evalComparison(String op, Result left, Result right) {
        BiFunction<Double, Double, Boolean> binary;
        switch (op) {
            case "<":
                binary = (a, b) -> a < b;
                break;
            case "<=":
                binary = (a, b) -> a <= b;
                break;
            case ">":
                binary = (a, b) -> a > b;
                break;
            case ">=":
                binary = (a, b) -> a >= b;
                break;
            case "==":
                binary = (a, b) -> a.equals(b);
                break;
            case "!=":
                binary = (a, b) -> !a.equals(b);
                break;
            default:
                throw new AssertionError();
        }
        return left
            .typecheck(DOUBLE)
            .flatMap(l ->
                right
                    .typecheck(DOUBLE)
                    .flatMap(r -> Result.success(
                        Value.of(binary.apply(
                            l.getAsDouble(),
                            r.getAsDouble()
                        ))
                    ))
            );
    }

    private boolean isArithmetic(String op) {
        return List.of("+", "-", "*", "/").contains(op);
    }

    private boolean isLogical(String op) {
        return List.of("&&", "||").contains(op);
    }

    private boolean isComparison(String op) {
        return List.of("<", "<=", ">", ">=", "==", "!=").contains(op);
    }

    @Override
    public Result visit(Conditional conditional) {
        return conditional.getCondition().accept(this)
            .typecheck(Type.BOOLEAN)
            .flatMap(v -> (
                    v.getAsBoolean()
                    ? conditional.getThenClause()
                    : conditional.getElseClause()
                ).accept(this)
            );
    }

    @Override
    public Result visit(Function function) {
        String name = function.getName();
        List<Expression> args = function.getArgs();
        if (name.equals("pow"))
            return evalPow(args);
        if (name.equals("length"))
            return evalLength(args);
        if (name.equals("sum"))
            return evalSum(args);
        return Result.failure("Unknown function: " + name);
    }

    private Result evalPow(List<Expression> args) {
        if (args.size() != 2)
            return Result.failure(wrongNumberOfArgumentsMessage("pow"));
        return args.get(0).accept(this)
            .typecheck(DOUBLE)
            .flatMap(base ->
                args.get(1).accept(this)
                    .typecheck(DOUBLE)
                    .flatMap(exp -> Result.success(
                        Value.of(Math.pow(
                            base.getAsDouble(),
                            exp.getAsDouble()
                        ))
                    ))
            );
    }

    private Result evalLength(List<Expression> args) {
        if (args.size() != 1)
            return Result.failure(wrongNumberOfArgumentsMessage("length"));
        return args.get(0).accept(this)
            .typecheck(STRING)
            .map(v -> Value.of((double) v.getAsString().length()));
    }

    private Result evalSum(List<Expression> args) {
        if (args.size() != 1)
            return Result.failure(wrongNumberOfArgumentsMessage("sum"));
        return args.get(0).accept(this)
            .typecheck(RANGE)
            .flatMap(range -> StreamSupport
                .stream(range.getAsRange().spliterator(), false)
                .map(model::getResultAt)
                .map(r -> r.typecheck(DOUBLE))
                .reduce(
                    Result.success(Value.of(0.)),
                    (acc, e) -> Result.combine(acc, e, (accVal, eVal) ->
                        Value.of(accVal.getAsDouble() + eVal.getAsDouble())
                    )
                )
            );
    }

    private String wrongNumberOfArgumentsMessage(String name) {
        return "Wrong number of arguments for function: " + name;
    }

    @Override
    public Result visit(BooleanLiteral literal) {
        return Result.success(Value.of(literal.getValue()));
    }

    @Override
    public Result visit(DoubleLiteral literal) {
        return Result.success(Value.of(literal.getValue()));
    }

    @Override
    public Result visit(StringLiteral literal) {
        return Result.success(Value.of(literal.getValue()));
    }

    @Override
    public Result visit(Range range) {
        Reference first = range.getFirst();
        Reference last = range.getLast();
        if (!first.isResolved())
            return Result.failure(unresolvedMessage(first));
        if (!last.isResolved())
            return Result.failure(unresolvedMessage(last));
        Cell firstCell = first.getCell();
        Cell lastCell = last.getCell();
        if (firstCell.getRow() > lastCell.getRow()
            || firstCell.getColumn() > lastCell.getColumn())
        {
            return Result.failure("Incorrect range: " + range);
        }
        RangeValue result = new RangeValue(firstCell, lastCell, range.toString());
        return Result.success(Value.of(result));
    }

    @Override
    public Result visit(Reference reference) {
        if (!reference.isResolved())
            return Result.failure(unresolvedMessage(reference));
        return model.getResultAt(reference.getCell());
    }

    private String unresolvedMessage(Reference reference) {
        return String.format("Reference %s unresolved", reference.getName());
    }
}
