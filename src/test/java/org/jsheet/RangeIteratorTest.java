package org.jsheet;

import org.jsheet.model.JSheetCell;
import org.jsheet.model.RangeIterator;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class RangeIteratorTest {
    @Test
    public void iterator() {
        JSheetCell first = new JSheetCell(1, 1);
        JSheetCell last = new JSheetCell(3, 3);
        Iterator<JSheetCell> iterator = new RangeIterator(first, last);
        List<JSheetCell> sequence = new ArrayList<>();
        while (iterator.hasNext()) {
            sequence.add(iterator.next());
        }
        List<JSheetCell> expected = Arrays.asList(
            new JSheetCell(1, 1), new JSheetCell(1, 2), new JSheetCell(1, 3),
            new JSheetCell(2, 1), new JSheetCell(2, 2), new JSheetCell(2, 3),
            new JSheetCell(3, 1), new JSheetCell(3, 2), new JSheetCell(3, 3)
        );
        assertEquals(expected, sequence);
        assertThrows(NoSuchElementException.class, iterator::next);
    }
}
