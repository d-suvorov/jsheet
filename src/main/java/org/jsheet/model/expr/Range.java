package org.jsheet.model.expr;

import org.jsheet.model.*;

import java.util.Iterator;

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
        RangeValue range = new RangeValue(first.getCell(), last.getCell());
        return Result.success(Value.of(range));
    }

    public boolean isResolved() {
        return first.isResolved() && last.isResolved();
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
