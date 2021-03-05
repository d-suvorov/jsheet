package org.jsheet.expression;

import org.jsheet.data.Cell;
import org.jsheet.data.JSheetTableModel;
import org.jsheet.data.Result;
import org.jsheet.data.Value;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class Reference extends Expression {
    private static final Pattern REFERENCE_PATTERN
        = Pattern.compile("(\\$)?([a-zA-Z]+)(\\$)?(\\d+)");

    private final String name;
    private Cell cell;
    private boolean isRowAbsolute = false;
    private boolean isColumnAbsolute = false;

    public Reference(String name) {
        this.name = name;
    }

    public Reference(String name, Cell cell, boolean isRowAbsolute, boolean isColumnAbsolute) {
        this.name = name;
        this.cell = cell;
        this.isRowAbsolute = isRowAbsolute;
        this.isColumnAbsolute = isColumnAbsolute;
    }

    @Override
    public Value eval(JSheetTableModel model) throws EvaluationException {
        if (!isResolved())
            throw new EvaluationException(unresolvedMessage());
        Result result = model.getResultAt(cell);
        if (!result.isPresent())
            throw new EvaluationException(result.message());
        return result.get();
    }

    public String unresolvedMessage() {
        return String.format("Reference %s unresolved", name);
    }

    public boolean isResolved() {
        return cell != null;
    }

    public Cell getCell() {
        return cell;
    }

    public void resolve(JSheetTableModel model) {
        if (!isResolved())
            cell = resolve(name, model);
    }

    public Cell resolve(String name, JSheetTableModel model) {
        Matcher matcher = REFERENCE_PATTERN.matcher(name);
        if (!matcher.matches())
            return null;

        if (matcher.group(1) != null)
            isColumnAbsolute = true;

        String column = matcher.group(2);
        int columnIndex = model.findColumn(column);
        if (columnIndex == -1)
            return null;

        if (matcher.group(3) != null)
            isRowAbsolute = true;

        String row = matcher.group(4);
        int rowIndex = Integer.parseInt(row);
        if (rowIndex >= model.getRowCount())
            return null;

        return new Cell(rowIndex, columnIndex);
    }

    @Override
    public Reference shift(JSheetTableModel model, int rowShift, int columnShift) {
        if (!isResolved()) {
            // Leave unresolved references as-is. They only hold
            // a string value, thus it's perfectly fine to re-use them
            return this;
        }
        if (isRowAbsolute) rowShift = 0;
        if (isColumnAbsolute) columnShift = 0;
        Cell shiftedCell = new Cell(cell.row + rowShift, cell.column + columnShift);
        if (shiftedCell.row >= model.getRowCount() || shiftedCell.column >= model.getColumnCount())
            throw new IllegalStateException();
        String shiftedName = (isColumnAbsolute ? "$" : "")
            + model.getColumnName(shiftedCell.column)
            + (isRowAbsolute ? "$" : "")
            + shiftedCell.row;
        return new Reference(shiftedName, shiftedCell, isRowAbsolute, isColumnAbsolute);
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
