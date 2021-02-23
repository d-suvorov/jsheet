package org.jsheet;

import org.jsheet.model.JSheetTableModel;
import org.jsheet.model.Result;
import org.jsheet.model.Value;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

// TODO write more clear tests?
@SuppressWarnings("SameParameterValue")
public class TableModelTest {
    public static JSheetTableModel model;

    @BeforeAll
    static void setUp() {
        model = new JSheetTableModel();
    }

    @BeforeEach
    void clearRowZero() {
        for (int column = 0; column < model.getColumnCount(); column++) {
            model.setValueAt(null, 0, column);
        }
    }

    @Nested
    class ReadWrite {
        @Test
        void simpleReadWrite() {
            String dVal = "42";
            String strVal = "abc";
            model.setValueAt(dVal, 0, 0);
            model.setValueAt(strVal, 0, 1);
            assertEquals(Double.parseDouble(dVal), model.getValueAt(0, 0).getAsDouble());
            assertEquals(strVal, model.getValueAt(0, 1).getAsString());
        }
    }

    private void testDoubleValuedFormula(String formula, double expectedResult) {
        Result result = testFormulaImpl(formula);
        assertEquals(expectedResult, result.get().getAsDouble(), 0);
    }

    private void testBooleanValuedFormula(String formula, boolean expectedResult) {
        Result result = testFormulaImpl(formula);
        assertEquals(expectedResult, result.get().getAsBoolean());
    }

    private Result testFormulaImpl(String formula) {
        model.setValueAt(formula, 0, 0);
        Value value = model.getValueAt(0, 0);
        assertSame(Value.Type.EXPR, value.getTag());
        return value.getAsExpr().getResult();
    }

    @Nested
    class Arithmetic {
        @Test
        public void simpleExpression1() {
            testDoubleValuedFormula("= 1 + 2 * 3", 1 + 2 * 3);
        }

        @Test
        public void simpleExpression2() {
            testDoubleValuedFormula("= (1 + 2) * 3", (1 + 2) * 3);
        }

        @Test
        public void divisionByZero() {
            testDoubleValuedFormula("= 42 / 0", Double.POSITIVE_INFINITY);
        }
    }

    @Nested
    class Comparison {
        @Test
        public void less() {
            testBooleanValuedFormula("= 1 < 2", true);
        }

        @Test
        public void lessOrEqual() {
            testBooleanValuedFormula("= 1 <= 2", true);
        }

        @Test
        public void greater() {
            testBooleanValuedFormula("= 1 > 2", false);
        }

        @Test
        public void greaterOrEqual() {
            testBooleanValuedFormula("= 1 >= 2", false);
        }
    }

    @Nested
    class Logical {
        @Test
        public void and() {
            testBooleanValuedFormula("= false && true", false);
        }

        @Test
        public void or() {
            testBooleanValuedFormula("= false || true", true);
        }

        @Test
        public void simple() {
            testBooleanValuedFormula("= 1 > 2 || 1 != 42", true);
        }
    }

    @Nested
    class Conditional {
        @Test
        public void conditional() {
            testDoubleValuedFormula("= if true then 42 else 0", 42);
            testDoubleValuedFormula("= if false then 42 else 0", 0);
        }
    }

    @Nested
    class Functions {
        @Test
        public void pow() {
            testDoubleValuedFormula("= pow(2, 4)", Math.pow(2, 4));
        }

        @Test
        public void length() {
            testDoubleValuedFormula("= length(\"abracadabra\")", "abracadabra".length());
        }

        @Test
        public void lengthRef() {
            model.setValueAt("abracadabra", 0, 0);
            model.setValueAt("= length(A0)", 0, 1);
            Value value = model.getValueAt(0, 1);
            assertSame(Value.Type.EXPR, value.getTag());
            Result result = value.getAsExpr().getResult();
            assertEquals("abracadabra".length(), result.get().getAsDouble(), 0);
        }

        @Test
        public void undefined() {
            model.setValueAt("= abracadabra(2, 4)", 0, 0);
            Value value = model.getValueAt(0, 0);
            assertSame(Value.Type.EXPR, value.getTag());
            Result result = value.getAsExpr().getResult();
            assertFalse(result.isPresent());
            assertEquals("Unknown function: abracadabra", result.message());
        }
    }

    @Nested
    class EvaluationWithReferences {
        @Test
        void simpleReferences() {
            double val = 42;
            model.setValueAt(Double.toString(val), 0, 0);
            model.setValueAt("= A0 + 1", 0, 1);
            model.setValueAt("= B0 - 1", 0, 2);
            checkSuccessResult(val + 1, 0, 1);
            checkSuccessResult(val, 0, 2);
            val += 0.5;
            model.setValueAt(Double.toString(val), 0, 0);
            checkSuccessResult(val + 1, 0, 1);
            checkSuccessResult(val, 0, 2);
        }

        @Test
        void dependenciesSimpleCycle() {
            double val = 42;
            // A0:C0 is a cycle
            model.setValueAt("= C0", 0, 0);
            model.setValueAt("= A0", 0, 1);
            model.setValueAt("= B0", 0, 2);
            // D0 -> C0
            model.setValueAt("= C0", 0, 3);
            checkErrorResult("Circular dependency", 0, 0);
            checkErrorResult("Circular dependency", 0, 1);
            checkErrorResult("Circular dependency", 0, 2);
            checkErrorResult("Circular dependency", 0, 3);
            // Break the cycle
            model.setValueAt(Double.toString(val), 0, 2);
            checkSuccessResult(val, 0, 0);
            checkSuccessResult(val, 0, 1);
            checkPlainDouble(val, 0, 2);
            checkSuccessResult(val, 0, 3);
        }

        @Test
        void dependenciesLoop() {
            model.setValueAt("=A0", 0, 0);
            checkErrorResult("Circular dependency", 0, 0);
        }

        @Test
        void dependenciesTwoCycles() {
            double val = 42;
            // A0:C0 is a cycle
            model.setValueAt("= C0", 0, 0);
            model.setValueAt("= A0", 0, 1);
            model.setValueAt("= B0", 0, 2);
            // E0:C0 is a cycle
            model.setValueAt("= D0", 0, 2);
            model.setValueAt("= E0", 0, 3);
            model.setValueAt("= C0", 0, 4);
            checkErrorResult("Circular dependency", 0, 0);
            checkErrorResult("Circular dependency", 0, 1);
            checkErrorResult("Circular dependency", 0, 2);
            checkErrorResult("Circular dependency", 0, 3);
            checkErrorResult("Circular dependency", 0, 4);
            // Break the cycle
            model.setValueAt(null, 0, 2);
            checkErrorResult("Cell C0 is uninitialized", 0, 0);
            checkErrorResult("Cell C0 is uninitialized", 0, 1);
            checkErrorResult("Cell C0 is uninitialized", 0, 3);
            checkErrorResult("Cell C0 is uninitialized", 0, 4);
            model.setValueAt(Double.toString(42), 0, 2);
            checkSuccessResult(val, 0, 0);
            checkSuccessResult(val, 0, 1);
            checkSuccessResult(val, 0, 3);
            checkSuccessResult(val, 0, 4);
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
}
