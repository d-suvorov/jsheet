package org.jsheet.expression;

import org.jsheet.data.Cell;
import org.jsheet.data.JSheetTableModel;
import org.jsheet.expression.evaluation.Result;
import org.jsheet.expression.evaluation.Value;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class Reference extends Expression {
    private static final Pattern REFERENCE_PATTERN
        = Pattern.compile("(\\$)?([a-zA-Z]+)(\\$)?(\\d+)");
    public static final String OUT_OF_BOUNDS_REFERENCE_NAME = "REF";

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
    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visit(this);
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
    public Stream<Reference> getReferences() {
        return Stream.of(this);
    }

    @Override
    public Stream<Range> getRanges() {
        return Stream.empty();
    }

    public boolean isRowAbsolute() {
        return isRowAbsolute;
    }

    public boolean isColumnAbsolute() {
        return isColumnAbsolute;
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
