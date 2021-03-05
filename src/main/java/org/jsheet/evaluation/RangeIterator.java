package org.jsheet.evaluation;

import org.jsheet.data.Cell;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class RangeIterator implements Iterator<Cell> {
    private final Cell firstCell;
    private final Cell lastCell;

    private int currentRow;
    private int currentColumn;

    public RangeIterator(Cell firstCell, Cell lastCell) {
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
    public Cell next() {
        if (!hasNext())
            throw new NoSuchElementException();
        Cell next = new Cell(currentRow, currentColumn);
        if (currentColumn < lastCell.getColumn()) {
            currentColumn++;
        } else { // currentColumn == lastCell.getColumn()
            currentColumn = firstCell.getColumn();
            currentRow++;
        }
        return next;
    }
}
