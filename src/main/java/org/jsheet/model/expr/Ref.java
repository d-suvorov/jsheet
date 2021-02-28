package org.jsheet.model.expr;

import org.jsheet.model.JSheetCell;
import org.jsheet.model.JSheetTableModel;
import org.jsheet.model.Result;

import java.util.Objects;
import java.util.stream.Stream;

public class Ref extends Expr {
    private final String name;
    private JSheetCell cell;

    public Ref(String name) {
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

    public JSheetCell getCell() {
        return cell;
    }

    public void resolve(JSheetTableModel model) {
        if (!isResolved())
            cell = model.resolveRef(name);
    }

    @Override
    public Ref shift(JSheetTableModel model, int rowShift, int columnShift) {
        if (!isResolved())
            throw new IllegalStateException();
        JSheetCell shiftedCell = new JSheetCell(cell.row + rowShift, cell.column + columnShift);
        if (shiftedCell.row >= model.getRowCount() || shiftedCell.column >= model.getColumnCount())
            throw new IllegalStateException();
        String shiftedName = model.getColumnName(shiftedCell.column) + shiftedCell.row;
        Ref shifted = new Ref(shiftedName);
        shifted.cell = shiftedCell;
        return shifted;
    }

    @Override
    public Stream<Ref> getRefs() {
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

        Ref ref = (Ref) o;

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
