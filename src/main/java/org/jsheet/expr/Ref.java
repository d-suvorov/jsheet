package org.jsheet.expr;

import org.jsheet.model.JSheetCell;
import org.jsheet.model.JSheetTableModel;

import java.util.Map;

public class Ref extends Expr {
    private final String name;

    public Ref(String name) {
        this.name = name;
    }

    @Override
    public double eval(JSheetTableModel model, Map<String, JSheetCell> refToCell) {
        JSheetCell cell = refToCell.get(name);
        if (cell != null) {
            return model.eval(cell);
        }
        throw new AssertionError("unimplemented");
    }

    @Override
    public String toString() {
        return name;
    }
}
