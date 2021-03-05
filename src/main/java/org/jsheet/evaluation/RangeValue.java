package org.jsheet.evaluation;

import org.jsheet.data.Cell;

import java.util.Iterator;

public class RangeValue implements Iterable<Cell> {
    private final Cell first;
    private final Cell last;
    private final String name;

    public RangeValue(Cell first, Cell last, String name) {
        this.first = first;
        this.last = last;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public Iterator<Cell> iterator() {
        return new RangeIterator(first, last);
    }
}
