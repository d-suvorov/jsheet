package org.jsheet.model;

import java.util.Objects;

public class JSheetCell {
    public final int row;
    public final int column;

    public JSheetCell(int row, int column) {
        this.row = row;
        this.column = column;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JSheetCell that = (JSheetCell) o;
        return row == that.row && column == that.column;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, column);
    }

    @Override
    public String toString() {
        return String.format("(%d, %d)", row, column);
    }
}
