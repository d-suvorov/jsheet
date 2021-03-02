package org.jsheet.model.expression;

import org.jsheet.model.Cell;
import org.jsheet.model.JSheetTableModel;
import org.jsheet.model.Result;

import java.util.Objects;
import java.util.stream.Stream;

public class Reference extends Expression {
    private final String name;
    private Cell cell;

    public Reference(String name) {
        this.name = name;
    }

    @Override
    public Result eval(JSheetTableModel model) {
        if (!isResolved())
            return Result.failure(unresolvedMessage());
        return model.getResultAt(cell);
    }

    public boolean isResolved() {
        return cell != null;
    }

    public String unresolvedMessage() {
        return String.format("Reference %s unresolved", name);
    }

    public Cell getCell() {
        return cell;
    }

    public void resolve(JSheetTableModel model) {
        if (!isResolved())
            cell = model.resolveReference(name);
    }

    @Override
    public Reference shift(JSheetTableModel model, int rowShift, int columnShift) {
        if (!isResolved()) {
            // Leave unresolved references as-is. They only hold
            // a string value, thus it's perfectly fine to re-use them
            return this;
        }
        Cell shiftedCell = new Cell(cell.row + rowShift, cell.column + columnShift);
        if (shiftedCell.row >= model.getRowCount() || shiftedCell.column >= model.getColumnCount())
            throw new IllegalStateException();
        String shiftedName = model.getColumnName(shiftedCell.column) + shiftedCell.row;
        Reference shifted = new Reference(shiftedName);
        shifted.cell = shiftedCell;
        return shifted;
    }

    @Override
    public Stream<Reference> getReferences() {
        return Stream.of(this);
    }

    @Override
    public Stream<Range> getRanges() {
        return Stream.empty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Reference ref = (Reference) o;

        return Objects.equals(name, ref.name);
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    @Override
    public String toString() {
        return name;
    }
}
