package org.jsheet;

import org.jsheet.model.JSheetTableModel;
import org.jsheet.model.Result;
import org.jsheet.model.Value;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

// TODO write more clear tests?
@SuppressWarnings("SameParameterValue")
public class TableModelTest {
    public static JSheetTableModel model;

    @BeforeAll
    static void setUp() {
        model = new JSheetTableModel();
    }

    @Test
    void simpleReadWrite() {
        String dVal = "42";
        String strVal = "abc";
        model.setValueAt(dVal, 0, 0);
        model.setValueAt(strVal, 0, 1);
        assertEquals(Double.parseDouble(dVal), model.getValueAt(0, 0).getAsDouble());
        assertEquals(strVal, model.getValueAt(0, 1).getAsString());
    }

    @Test
    void simpleReferences() {
        double val = 42;
        model.setValueAt(Double.toString(val), 1, 0);
        model.setValueAt("= A1 + 1", 1, 1);
        model.setValueAt("= B1 - 1", 1, 2);
        checkSuccessResult(val + 1, 1, 1);
        checkSuccessResult(val, 1, 2);
        val += 0.5;
        model.setValueAt(Double.toString(val), 1, 0);
        checkSuccessResult(val + 1, 1, 1);
        checkSuccessResult(val, 1, 2);
    }

    @Test
    void dependenciesSimpleCycle() {
        double val = 42;
        // A2:C2 is a cycle
        model.setValueAt("= C2", 2, 0);
        model.setValueAt("= A2", 2, 1);
        model.setValueAt("= B2", 2, 2);
        // D2 -> C2
        model.setValueAt("= C2", 2, 3);
        checkErrorResult("Circular dependency", 2, 0);
        checkErrorResult("Circular dependency", 2, 1);
        checkErrorResult("Circular dependency", 2, 2);
        checkErrorResult("Circular dependency", 2, 3);
        // Break the cycle
        model.setValueAt(Double.toString(val), 2, 2);
        checkSuccessResult(val, 2, 0);
        checkSuccessResult(val, 2, 1);
        checkPlainDouble(val, 2, 2);
        checkSuccessResult(val, 2, 3);
    }

    @Test
    void dependenciesLoop() {
        model.setValueAt("=A3", 3, 0);
        checkErrorResult("Circular dependency", 3, 0);
    }

    @Test
    void dependenciesTwoCycles() {
        double val = 42;
        // A4:C4 is a cycle
        model.setValueAt("= C4", 4, 0);
        model.setValueAt("= A4", 4, 1);
        model.setValueAt("= B4", 4, 2);
        // E4:C4 is a cycle
        model.setValueAt("= D4", 4, 2);
        model.setValueAt("= E4", 4, 3);
        model.setValueAt("= C4", 4, 4);
        checkErrorResult("Circular dependency", 4, 0);
        checkErrorResult("Circular dependency", 4, 1);
        checkErrorResult("Circular dependency", 4, 2);
        checkErrorResult("Circular dependency", 4, 3);
        checkErrorResult("Circular dependency", 4, 4);
        // Break the cycle
        model.setValueAt("", 4, 2);
        checkErrorResult("Wrong value type in the cell C4", 4, 0);
        checkErrorResult("Wrong value type in the cell C4", 4, 1);
        checkErrorResult("Wrong value type in the cell C4", 4, 3);
        checkErrorResult("Wrong value type in the cell C4", 4, 4);
        model.setValueAt(Double.toString(42), 4, 2);
        checkSuccessResult(val, 4, 0);
        checkSuccessResult(val, 4, 1);
        checkSuccessResult(val, 4, 3);
        checkSuccessResult(val, 4, 4);
    }

    private void checkPlainDouble(double expected, int row, int column) {
        Value val = model.getValueAt(row, column);
        assertSame(val.getTag(), Value.Type.DOUBLE);
        assertEquals(expected, val.getAsDouble(), 0);
    }

    private void checkSuccessResult(double expected, int row, int column) {
        Value val = model.getValueAt(row, column);
        assertSame(val.getTag(), Value.Type.EXPR);
        Result result = val.getAsExpr().getResult();
        assertEquals(expected, result.get().getAsDouble(), 0);
    }

    private void checkErrorResult(String expected, int row, int column) {
        Value val = model.getValueAt(row, column);
        assertSame(val.getTag(), Value.Type.EXPR);
        Result result = val.getAsExpr().getResult();
        assertEquals(expected, result.message());
    }
}
