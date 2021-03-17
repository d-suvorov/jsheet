package org.jsheet.expression;

public interface ExpressionVisitor<R> {
    R visit(Binop binop);
    R visit(Conditional conditional);
    R visit(Function function);
    R visit(Literal literal);
    R visit(Range range);
    R visit(Reference reference);
}
