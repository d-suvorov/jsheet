package org.jsheet;

import org.jsheet.data.Cell;
import org.jsheet.data.RangeIterator;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class RangeIteratorTest {
    @Test
    public void iterator() {
        Cell first = new Cell(1, 1);
        Cell last = new Cell(3, 3);
        Iterator<Cell> iterator = new RangeIterator(first, last);
        List<Cell> sequence = new ArrayList<>();
        while (iterator.hasNext()) {
            sequence.add(iterator.next());
        }
        List<Cell> expected = Arrays.asList(
            new Cell(1, 1), new Cell(1, 2), new Cell(1, 3),
            new Cell(2, 1), new Cell(2, 2), new Cell(2, 3),
            new Cell(3, 1), new Cell(3, 2), new Cell(3, 3)
        );
        assertEquals(expected, sequence);
        assertThrows(NoSuchElementException.class, iterator::next);
    }
}
