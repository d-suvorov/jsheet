package org.jsheet.expression.evaluation;

import org.jsheet.expression.*;

public interface EvaluationVisitor<R> {
    R visit(Binop binop) throws EvaluationException;
    R visit(Conditional conditional) throws EvaluationException;
    R visit(Function function) throws EvaluationException;
    R visit(BooleanLiteral literal) throws EvaluationException;
    R visit(DoubleLiteral literal) throws EvaluationException;
    R visit(StringLiteral literal) throws EvaluationException;
    R visit(Range range) throws EvaluationException;
    R visit(Reference reference) throws EvaluationException;
}
