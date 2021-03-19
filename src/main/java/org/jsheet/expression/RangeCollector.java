package org.jsheet.expression;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RangeCollector implements ExpressionVisitor<Stream<Range>> {
    @Override
    public Stream<Range> visit(Binop binop) {
        return Stream.concat(
            binop.getLeft().accept(this),
            binop.getRight().accept(this)
        );
    }

    @Override
    public Stream<Range> visit(Conditional conditional) {
        return Stream.of(
            conditional.getCondition(),
            conditional.getThenClause(),
            conditional.getElseClause()
        ).flatMap(e -> e.accept(this));
    }

    @Override
    public Stream<Range> visit(Function function) {
        return function.getArgs().stream().flatMap(e -> e.accept(this));
    }

    @Override
    public Stream<Range> visit(BooleanLiteral literal) {
        return Stream.empty();
    }

    @Override
    public Stream<Range> visit(DoubleLiteral literal) {
        return Stream.empty();
    }

    @Override
    public Stream<Range> visit(StringLiteral literal) {
        return Stream.empty();
    }

    @Override
    public Stream<Range> visit(Range range) {
        return Stream.of(range);
    }

    @Override
    public Stream<Range> visit(Reference reference) {
        return Stream.empty();
    }

    public static List<Range> getRanges(Expression expression) {
        return expression.accept(new RangeCollector()).collect(Collectors.toList());
    }
}
