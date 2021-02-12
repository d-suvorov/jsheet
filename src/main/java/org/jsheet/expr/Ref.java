package org.jsheet.expr;

import org.jsheet.model.JSheetCell;
import org.jsheet.model.JSheetTableModel;
import org.jsheet.model.Result;

import java.util.Map;

public class Ref extends Expr {
    private final String name;

    public Ref(String name) {
        this.name = name;
    }

    @Override
    public Result eval(JSheetTableModel model, Map<String, JSheetCell> refToCell) {
        JSheetCell cell = refToCell.get(name);
        if (cell != null) {
            return model.eval(cell);
        }
        return Result.failure(String.format("Reference %s unresolved", name));
    }

    @Override
    public String toString() {
        return name;
    }
}
