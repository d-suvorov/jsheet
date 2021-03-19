package org.jsheet.expression;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ReferenceCollector implements ExpressionVisitor<Stream<Reference>> {
    @Override
    public Stream<Reference> visit(Binop binop) {
        return Stream.concat(
            binop.getLeft().accept(this),
            binop.getRight().accept(this)
        );
    }

    @Override
    public Stream<Reference> visit(Conditional conditional) {
        return Stream.of(
            conditional.getCondition(),
            conditional.getThenClause(),
            conditional.getElseClause()
        ).flatMap(e -> e.accept(this));
    }

    @Override
    public Stream<Reference> visit(Function function) {
        return function.getArgs().stream().flatMap(e -> e.accept(this));
    }

    @Override
    public Stream<Reference> visit(BooleanLiteral literal) {
        return Stream.empty();
    }

    @Override
    public Stream<Reference> visit(DoubleLiteral literal) {
        return Stream.empty();
    }

    @Override
    public Stream<Reference> visit(StringLiteral literal) {
        return Stream.empty();
    }

    @Override
    public Stream<Reference> visit(Range range) {
        return Stream.of(range.getFirst(), range.getLast());
    }

    @Override
    public Stream<Reference> visit(Reference reference) {
        return Stream.of(reference);
    }

    public static List<Reference> getReferences(Expression expression) {
        return expression.accept(new ReferenceCollector()).collect(Collectors.toList());
    }
}
