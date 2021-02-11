package org.jsheet.expr;

import org.jsheet.model.JSheetCell;
import org.jsheet.model.JSheetTableModel;

import java.util.Map;

public class Const extends Expr {
    private final double value;

    public Const(double value) {
        this.value = value;
    }

    @Override
    public double eval(JSheetTableModel model, Map<String, JSheetCell> refToCell) {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
