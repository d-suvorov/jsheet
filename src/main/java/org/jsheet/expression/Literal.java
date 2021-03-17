package org.jsheet.expression;

import org.jsheet.data.JSheetTableModel;
import org.jsheet.expression.evaluation.Type;
import org.jsheet.expression.evaluation.Value;

import java.util.Objects;
import java.util.stream.Stream;

public class Literal extends Expression {
    private final Value value;

    public Literal(Value value) {
        if (value.getTag() == Type.FORMULA)
            throw new IllegalArgumentException();
        this.value = value;
    }

    @Override
    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public Value eval(JSheetTableModel model) {
        return value;
    }

    @Override
    public Stream<Reference> getReferences() {
        return Stream.empty();
    }

    @Override
    public Stream<Range> getRanges() {
        return Stream.empty();
    }

    public Value getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Literal literal = (Literal) o;

        return Objects.equals(value, literal.value);
    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
