package org.jsheet.expr;

import org.jsheet.model.JSheetCell;
import org.jsheet.model.JSheetTableModel;
import org.jsheet.model.Result;

import java.util.Map;

public abstract class Expr {
    public abstract Result eval(JSheetTableModel model, Map<String, JSheetCell> refToCell);
}
