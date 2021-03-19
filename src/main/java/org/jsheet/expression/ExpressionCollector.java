package org.jsheet.expression;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ExpressionCollector<T> implements ExpressionVisitor<Stream<T>> {
    @Override
    public Stream<T> visit(Binop binop) {
        return Stream.concat(
            binop.getLeft().accept(this),
            binop.getRight().accept(this)
        );
    }

    @Override
    public Stream<T> visit(Conditional conditional) {
        return Stream.of(
            conditional.getCondition(),
            conditional.getThenClause(),
            conditional.getElseClause()
        ).flatMap(e -> e.accept(this));
    }

    @Override
    public Stream<T> visit(Function function) {
        return function.getArgs().stream().flatMap(e -> e.accept(this));
    }

    @Override
    public Stream<T> visit(BooleanLiteral literal) {
        return Stream.empty();
    }

    @Override
    public Stream<T> visit(DoubleLiteral literal) {
        return Stream.empty();
    }

    @Override
    public Stream<T> visit(StringLiteral literal) {
        return Stream.empty();
    }

    @Override
    public Stream<T> visit(Range range) {
        return Stream.concat(
            range.getFirst().accept(this),
            range.getLast().accept(this)
        );
    }

    @Override
    public Stream<T> visit(Reference reference) {
        return Stream.empty();
    }

    public List<T> collect(Expression expression) {
        return expression.accept(this).collect(Collectors.toList());
    }
}
