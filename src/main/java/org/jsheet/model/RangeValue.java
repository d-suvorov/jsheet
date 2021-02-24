package org.jsheet.model;

import java.util.Iterator;

public class RangeValue implements Iterable<JSheetCell> {
    private final JSheetCell first;
    private final JSheetCell last;

    public RangeValue(JSheetCell first, JSheetCell last) {
        this.first = first;
        this.last = last;
    }

    @Override
    public Iterator<JSheetCell> iterator() {
        return new RangeIterator(first, last);
    }
}
