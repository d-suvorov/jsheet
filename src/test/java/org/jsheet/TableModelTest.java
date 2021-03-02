package org.jsheet;

import org.jsheet.model.JSheetTableModel;
import org.jsheet.model.Type;
import org.jsheet.model.Value;
import org.jsheet.parser.ParseException;
import org.junit.jupiter.api.*;

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
        clearRow(0);
    }

    private void clearRow(int rowIndex) {
        for (int column = 0; column < model.getColumnCount(); column++) {
            model.setValueAt(null, rowIndex, column);
        }
    }

    @Nested
    class ReadWrite {
        @Test
        void simpleReadWrite() throws ParseException {
            String dVal = "42";
            String strVal = "abc";
            TestUtils.setValue(model, dVal, 0, 0);
            TestUtils.setValue(model, strVal, 0, 1);
            assertEquals(Double.parseDouble(dVal), model.getValueAt(0, 0).getAsDouble());
            assertEquals(strVal, model.getValueAt(0, 1).getAsString());
        }
    }

    @Nested
    class Arithmetic {
        @Test
        public void simpleExpression1() throws ParseException {
            testDoubleValuedFormula("= 1 + 2 * 3", 1 + 2 * 3);
        }

        @Test
        public void simpleExpression2() throws ParseException {
            testDoubleValuedFormula("= (1 + 2) * 3", (1 + 2) * 3);
        }

        @Test
        public void divisionByZero() throws ParseException {
            testDoubleValuedFormula("= 42 / 0", Double.POSITIVE_INFINITY);
        }
    }

    @Nested
    class Comparison {
        @Test
        public void less() throws ParseException {
            testBooleanValuedFormula("= 1 < 2", true);
        }

        @Test
        public void lessOrEqual() throws ParseException {
            testBooleanValuedFormula("= 1 <= 2", true);
        }

        @Test
        public void greater() throws ParseException {
            testBooleanValuedFormula("= 1 > 2", false);
        }

        @Test
        public void greaterOrEqual() throws ParseException {
            testBooleanValuedFormula("= 1 >= 2", false);
        }
    }

    @Nested
    class Logical {
        @Test
        public void and() throws ParseException {
            testBooleanValuedFormula("= false && true", false);
        }

        @Test
        public void or() throws ParseException {
            testBooleanValuedFormula("= false || true", true);
        }

        @Test
        public void simple() throws ParseException {
            testBooleanValuedFormula("= 1 > 2 || 1 != 42", true);
        }
    }

    @Nested
    class Conditional {
        @Test
        public void conditional() throws ParseException {
            testDoubleValuedFormula("= if true then 42 else 0", 42);
            testDoubleValuedFormula("= if false then 42 else 0", 0);
        }
    }

    @Nested
    class Functions {
        @Test
        public void pow() throws ParseException {
            testDoubleValuedFormula("= pow(2, 4)", Math.pow(2, 4));
        }

        @Test
        public void length() throws ParseException {
            testDoubleValuedFormula("= length(\"abracadabra\")", "abracadabra".length());
        }

        @Test
        public void lengthByReference() throws ParseException {
            TestUtils.setValue(model, "abracadabra", 0, 0);
            TestUtils.setValue(model, "= length(A0)", 0, 1);
            Value value = model.getValueAt(0, 1);
            assertSame(Type.FORMULA, value.getTag());
            Value result = value.getAsFormula().getResult();
            assertEquals("abracadabra".length(), result.getAsDouble(), 0);
        }

        @Test
        public void undefined() throws ParseException {
            TestUtils.setValue(model, "= abracadabra(2, 4)", 0, 0);
            Value value = model.getValueAt(0, 0);
            assertSame(Type.FORMULA, value.getTag());
            Value result = value.getAsFormula().getResult();
            assertFalse(result.isPresent());
            assertEquals("Unknown function: abracadabra", result.getMessage());
        }
    }

    @Nested
    class Ranges {
        @AfterEach
        public void clear() {
            clearRow(0);
            clearRow(1);
            clearRow(2);
        }

        @Test
        public void simpleRangeSum() throws ParseException {
            TestUtils.setValue(model, "1", 0, 0);
            TestUtils.setValue(model, "2", 0, 1);
            TestUtils.setValue(model, "3", 0, 2);
            TestUtils.setValue(model, "1", 1, 0);
            TestUtils.setValue(model, "2", 1, 1);
            TestUtils.setValue(model, "3", 1, 2);
            TestUtils.setValue(model, "=sum(A0:C0)", 2, 0);
            TestUtils.setValue(model, "=sum(A1:C1)", 2, 1);
            TestUtils.setValue(model, "=sum(A0:C1)", 2, 2);
            checkSuccessDoubleResult(6, 2, 0);
            checkSuccessDoubleResult(6, 2, 1);
            checkSuccessDoubleResult(12, 2, 2);
        }

        @Test
        public void singletonRange() throws ParseException {
            double val = 42;
            TestUtils.setValue(model, Double.toString(val), 0, 0);
            TestUtils.setValue(model, "=sum(A0:A0)", 0, 1);
            checkSuccessDoubleResult(val, 0, 1);
        }

        @Test
        public void negativeRangeSum() throws ParseException {
            TestUtils.setValue(model, "1", 0, 0);
            TestUtils.setValue(model, "2", 0, 1);
            TestUtils.setValue(model, "3", 0, 2);
            TestUtils.setValue(model, "1", 1, 0);
            TestUtils.setValue(model, "2", 1, 1);
            TestUtils.setValue(model, "3", 1, 2);
            TestUtils.setValue(model, "=sum(C1:A0)", 2, 0);
            checkErrorResult("Incorrect range: C1:A0", 2, 0);
        }
    }

    @Nested
    class EvaluationWithReferences {
        @Test
        void simpleReferences() throws ParseException {
            double val = 42;
            TestUtils.setValue(model, Double.toString(val), 0, 0);
            TestUtils.setValue(model, "= A0 + 1", 0, 1);
            TestUtils.setValue(model, "= B0 - 1", 0, 2);
            checkSuccessDoubleResult(val + 1, 0, 1);
            checkSuccessDoubleResult(val, 0, 2);
            val += 0.5;
            TestUtils.setValue(model, Double.toString(val), 0, 0);
            checkSuccessDoubleResult(val + 1, 0, 1);
            checkSuccessDoubleResult(val, 0, 2);
        }

        @Test
        void dependenciesSimpleCycle() throws ParseException {
            double val = 42;
            // A0:C0 is a cycle
            TestUtils.setValue(model, "= C0", 0, 0);
            TestUtils.setValue(model, "= A0", 0, 1);
            TestUtils.setValue(model, "= B0", 0, 2);
            // D0 -> C0
            TestUtils.setValue(model, "= C0", 0, 3);
            checkErrorResult("Circular dependency", 0, 0);
            checkErrorResult("Circular dependency", 0, 1);
            checkErrorResult("Circular dependency", 0, 2);
            checkErrorResult("Circular dependency", 0, 3);
            // Break the cycle
            TestUtils.setValue(model, Double.toString(val), 0, 2);
            checkSuccessDoubleResult(val, 0, 0);
            checkSuccessDoubleResult(val, 0, 1);
            checkPlainDouble(val, 0, 2);
            checkSuccessDoubleResult(val, 0, 3);
        }

        @Test
        void dependenciesLoop() throws ParseException {
            TestUtils.setValue(model, "=A0", 0, 0);
            checkErrorResult("Circular dependency", 0, 0);
        }

        @Test
        void dependenciesTwoCycles() throws ParseException {
            double val = 42;
            // A0:C0 is a cycle
            TestUtils.setValue(model, "= C0", 0, 0);
            TestUtils.setValue(model, "= A0", 0, 1);
            TestUtils.setValue(model, "= B0", 0, 2);
            // E0:C0 is a cycle
            TestUtils.setValue(model, "= D0", 0, 2);
            TestUtils.setValue(model, "= E0", 0, 3);
            TestUtils.setValue(model, "= C0", 0, 4);
            checkErrorResult("Circular dependency", 0, 0);
            checkErrorResult("Circular dependency", 0, 1);
            checkErrorResult("Circular dependency", 0, 2);
            checkErrorResult("Circular dependency", 0, 3);
            checkErrorResult("Circular dependency", 0, 4);
            // Break the cycle
            TestUtils.setValue(model, null, 0, 2);
            checkErrorResult("Cell C0 is uninitialized", 0, 0);
            checkErrorResult("Cell C0 is uninitialized", 0, 1);
            checkErrorResult("Cell C0 is uninitialized", 0, 3);
            checkErrorResult("Cell C0 is uninitialized", 0, 4);
            TestUtils.setValue(model, Double.toString(42), 0, 2);
            checkSuccessDoubleResult(val, 0, 0);
            checkSuccessDoubleResult(val, 0, 1);
            checkSuccessDoubleResult(val, 0, 3);
            checkSuccessDoubleResult(val, 0, 4);
        }

        @Test
        void sameReferences() throws ParseException {
            double val = 42;
            TestUtils.setValue(model, Double.toString(val), 0, 0);
            TestUtils.setValue(model, "=A0 + A0", 0, 1);
            checkSuccessDoubleResult(val + val, 0, 1);
        }

        @Test
        void sameReferencesLoop() throws ParseException {
            TestUtils.setValue(model, "=A0 + A0", 0, 0);
            checkErrorResult("Circular dependency", 0, 0);
        }
    }

    private void checkPlainDouble(double expected, int row, int column) {
        Value val = model.getValueAt(row, column);
        assertSame(val.getTag(), Type.DOUBLE);
        assertEquals(expected, val.getAsDouble(), 0);
    }

    private void checkSuccessBooleanResult(boolean expected, int row, int column) {
        Value val = model.getValueAt(row, column);
        assertSame(val.getTag(), Type.FORMULA);
        Value result = val.getAsFormula().getResult();
        assertEquals(expected, result.getAsBoolean());
    }

    private void checkSuccessDoubleResult(double expected, int row, int column) {
        Value val = model.getValueAt(row, column);
        assertSame(val.getTag(), Type.FORMULA);
        Value result = val.getAsFormula().getResult();
        assertEquals(expected, result.getAsDouble(), 0);
    }

    private void checkErrorResult(String expected, int row, int column) {
        Value val = model.getValueAt(row, column);
        assertSame(val.getTag(), Type.FORMULA);
        Value result = val.getAsFormula().getResult();
        assertEquals(expected, result.getMessage());
    }

    private void testDoubleValuedFormula(String formula, double expectedResult)
        throws ParseException
    {
        TestUtils.setValue(model, formula, 0, 0);
        checkSuccessDoubleResult(expectedResult, 0, 0);
    }

    private void testBooleanValuedFormula(String formula, boolean expectedResult)
        throws ParseException
    {
        TestUtils.setValue(model, formula, 0, 0);
        checkSuccessBooleanResult(expectedResult, 0, 0);
    }
}
