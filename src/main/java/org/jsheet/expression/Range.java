package org.jsheet.expression;

import org.jsheet.data.*;

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
    public Result eval(JSheetTableModel model) {
        if (!first.isResolved())
            return Result.failure(first.unresolvedMessage());
        if (!last.isResolved())
            return Result.failure(last.unresolvedMessage());
        Cell firstCell = first.getCell();
        Cell lastCell = last.getCell();
        if (firstCell.getRow() > lastCell.getRow()
            || firstCell.getColumn() > lastCell.getColumn())
        {
            return Result.failure("Incorrect range: " + this);
        }
        RangeValue range = new RangeValue(firstCell, lastCell, toString());
        return Result.success(Value.of(range));
    }

    public boolean isResolved() {
        return first.isResolved() && last.isResolved();
    }

    @Override
    public Range shift(JSheetTableModel model, int rowShift, int columnShift) {
        Reference shiftedFirst = first.shift(model, rowShift, columnShift);
        Reference shiftedLast = last.shift(model, rowShift, columnShift);
        return new Range(shiftedFirst, shiftedLast);
    }

    @Override
    public Stream<Reference> getReferences() {
        return Stream.concat(first.getReferences(), last.getReferences());
    }

    @Override
    public Stream<Range> getRanges() {
        return Stream.of(this);
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
