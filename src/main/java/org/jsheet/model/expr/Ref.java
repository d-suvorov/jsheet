package org.jsheet.model.expr;

import org.jsheet.model.JSheetCell;
import org.jsheet.model.JSheetTableModel;
import org.jsheet.model.Result;

import java.util.Objects;

public class Ref extends Expr {
    private final String name;
    private JSheetCell cell;

    public Ref(String name) {
        this.name = name;
    }

    @Override
    public Result eval(JSheetTableModel model) {
        resolve(model);
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
