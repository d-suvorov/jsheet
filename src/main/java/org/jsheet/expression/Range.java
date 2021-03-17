package org.jsheet.expression;

import org.jsheet.data.*;
import org.jsheet.expression.evaluation.RangeValue;
import org.jsheet.expression.evaluation.Value;

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
    public Value eval(JSheetTableModel model) throws EvaluationException {
        if (!first.isResolved())
            throw new EvaluationException(first.unresolvedMessage());
        if (!last.isResolved())
            throw new EvaluationException(last.unresolvedMessage());
        Cell firstCell = first.getCell();
        Cell lastCell = last.getCell();
        if (firstCell.getRow() > lastCell.getRow()
            || firstCell.getColumn() > lastCell.getColumn())
        {
            throw new EvaluationException("Incorrect range: " + this);
        }
        RangeValue range = new RangeValue(firstCell, lastCell, toString());
        return Value.of(range);
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
