package org.jsheet.expression;

import org.jsheet.data.Cell;
import org.jsheet.evaluation.EvaluationException;
import org.jsheet.evaluation.EvaluationVisitor;

import java.util.Iterator;
import java.util.Objects;
import java.util.stream.Stream;

public class Range extends Expression implements Iterable<Cell> {
    private final Reference first;
    private final Reference last;

    public Range(Reference first, Reference last) {
        this.first = first;
        this.last = last;
    }

    @Override
    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public <R> R evaluate(EvaluationVisitor<R> visitor) throws EvaluationException {
        return visitor.visit(this);
    }

    public boolean isResolved() {
        return first.isResolved() && last.isResolved();
    }

    @Override
    public Stream<Reference> getReferences() {
        return Stream.concat(first.getReferences(), last.getReferences());
    }

    @Override
    public Stream<Range> getRanges() {
        return Stream.of(this);
    }

    public Reference getFirst() {
        return first;
    }

    public Reference getLast() {
        return last;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Range that = (Range) o;

        if (!Objects.equals(first, that.first)) return false;
        return Objects.equals(last, that.last);
    }

    @Override
    public int hashCode() {
        int result = first != null ? first.hashCode() : 0;
        result = 31 * result + (last != null ? last.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return String.format("%s:%s", first, last);
    }

    @Override
    public Iterator<Cell> iterator() {
        if (!isResolved())
            throw new IllegalStateException("unresolved range");
        return new RangeIterator(first.getCell(), last.getCell());
    }
}
