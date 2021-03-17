package org.jsheet.expression;

import org.jsheet.expression.evaluation.EvaluationException;
import org.jsheet.expression.evaluation.EvaluationVisitor;

import java.util.stream.Stream;

public abstract class Expression {
    public abstract <R> R accept(ExpressionVisitor<R> visitor);

    public abstract <R> R evaluate(EvaluationVisitor<R> visitor) throws EvaluationException;

    public abstract Stream<Reference> getReferences();

    public abstract Stream<Range> getRanges();
}
