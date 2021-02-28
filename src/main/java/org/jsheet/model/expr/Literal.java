package org.jsheet.model.expr;

import org.jsheet.model.JSheetTableModel;
import org.jsheet.model.Result;
import org.jsheet.model.Type;
import org.jsheet.model.Value;

import java.util.Objects;
import java.util.stream.Stream;

public class Literal extends Expr {
    private final Value value;

    public Literal(Value value) {
        if (value.getTag() == Type.EXPR)
            throw new IllegalArgumentException();
        this.value = value;
    }

    public Value getValue() {
        return value;
    }

    @Override
    public Result eval(JSheetTableModel model) {
        return Result.success(value);
    }

    @Override
    public Literal shift(JSheetTableModel model, int rowShift, int columnShift) {
        return this; // Plain values are immutable
    }

    @Override
    public Stream<Ref> getRefs() {
        return Stream.empty();
    }

    @Override
    public Stream<Range> getRanges() {
        return Stream.empty();
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
