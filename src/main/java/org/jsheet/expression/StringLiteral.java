package org.jsheet.expression;

import org.jsheet.expression.evaluation.EvaluationException;
import org.jsheet.expression.evaluation.EvaluationVisitor;

import java.util.Objects;

public class StringLiteral extends Literal {
    private final String value;

    public StringLiteral(String value) {
        this.value = value;
    }

    @Override
    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public <R> R evaluate(EvaluationVisitor<R> visitor) throws EvaluationException {
        return visitor.visit(this);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StringLiteral that = (StringLiteral) o;

        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }

    @Override
    public String toString() {
        return '"' + value + '"';
    }
}
