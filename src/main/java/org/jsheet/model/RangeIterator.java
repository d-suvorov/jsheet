package org.jsheet.model;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class RangeIterator implements Iterator<JSheetCell> {
    private final JSheetCell firstCell;
    private final JSheetCell lastCell;

    private int currentRow;
    private int currentColumn;

    public RangeIterator(JSheetCell firstCell, JSheetCell lastCell) {
        this.firstCell = firstCell;
        this.lastCell = lastCell;
        currentRow = firstCell.getRow();
        currentColumn = firstCell.getColumn();
    }

    @Override
    public boolean hasNext() {
        return currentRow <= lastCell.getRow()
            && currentColumn <= lastCell.getColumn();
    }

    @Override
    public JSheetCell next() {
        if (!hasNext())
            throw new NoSuchElementException();
        JSheetCell next = new JSheetCell(currentRow, currentColumn);
        if (currentColumn < lastCell.getColumn()) {
            currentColumn++;
        } else { // currentColumn == lastCell.getColumn()
            currentColumn = firstCell.getColumn();
            currentRow++;
        }
        return next;
    }
}
