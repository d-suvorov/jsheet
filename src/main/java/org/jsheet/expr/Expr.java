package org.jsheet.expr;

import org.jsheet.model.JSheetCell;
import org.jsheet.model.JSheetTableModel;

import java.util.Map;

public abstract class Expr {
    public abstract double eval(JSheetTableModel model, Map<String, JSheetCell> refToCell);
}
