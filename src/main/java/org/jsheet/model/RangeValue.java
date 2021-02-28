package org.jsheet.model;

import java.util.Iterator;

public class RangeValue implements Iterable<Cell> {
    private final Cell first;
    private final Cell last;

    public RangeValue(Cell first, Cell last) {
        this.first = first;
        this.last = last;
    }

    @Override
    public Iterator<Cell> iterator() {
        return new RangeIterator(first, last);
    }
}
