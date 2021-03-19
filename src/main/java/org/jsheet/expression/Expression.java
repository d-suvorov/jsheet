package org.jsheet.expression;

import org.jsheet.evaluation.EvaluationException;
import org.jsheet.evaluation.EvaluationVisitor;

import java.util.stream.Stream;

public abstract class Expression {
    public abstract <R> R accept(ExpressionVisitor<R> visitor);

    public abstract <R> R evaluate(EvaluationVisitor<R> visitor) throws EvaluationException;

    public abstract Stream<Range> getRanges();
}
