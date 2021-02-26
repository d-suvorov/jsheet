package org.jsheet.model.expr;

import org.jsheet.model.*;

import java.util.Iterator;
import java.util.Objects;

public class Range extends Expr implements Iterable<JSheetCell> {
    private final Ref first;
    private final Ref last;

    public Range(Ref first, Ref last) {
        this.first = first;
        this.last = last;
    }

    @Override
    public Result eval(JSheetTableModel model) {
        if (!first.isResolved())
            return Result.failure(first.unresolvedMessage());
        if (!last.isResolved())
            return Result.failure(last.unresolvedMessage());
        JSheetCell firstCell = first.getCell();
        JSheetCell lastCell = last.getCell();
        if (firstCell.getRow() > lastCell.getRow()
            || firstCell.getColumn() > lastCell.getColumn())
        {
            return Result.failure("Incorrect range: " + this);
        }
        RangeValue range = new RangeValue(firstCell, lastCell);
        return Result.success(Value.of(range));
    }

    public boolean isResolved() {
        return first.isResolved() && last.isResolved();
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
    public Iterator<JSheetCell> iterator() {
        if (!isResolved())
            throw new IllegalStateException("unresolved range");
        return new RangeIterator(first.getCell(), last.getCell());
    }
}
