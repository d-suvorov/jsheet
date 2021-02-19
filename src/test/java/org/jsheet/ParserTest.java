package org.jsheet;

import org.jsheet.model.ExprWrapper;
import org.jsheet.model.JSheetTableModel;
import org.jsheet.model.Result;
import org.jsheet.parser.ParserUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ParserTest {
    public static JSheetTableModel model;

    @BeforeAll
    static void setUp() {
        model = new JSheetTableModel();
    }

    @Test
    public void simpleExpression1() {
        ExprWrapper expr = ParserUtils.parse("= 1 + 2 * 3");
        assertEquals(1 + 2 * 3, expr.eval(model).get(), 0);
    }

    @Test
    public void simpleExpression2() {
        ExprWrapper expr = ParserUtils.parse("= (1 + 2) * 3");
        assertEquals((1 + 2) * 3, expr.eval(model).get(), 0);
    }

    @Test
    public void simpleExpression3() {
        ExprWrapper expr = ParserUtils.parse("= pow(2, 4)");
        assertEquals(Math.pow(2, 4), expr.eval(model).get(), 0);
    }

    @Test
    public void simpleExpression4() {
        ExprWrapper expr = ParserUtils.parse("= abracadabra(2, 4)");
        Result result = expr.eval(model);
        assertFalse(result.isPresent());
        assertEquals("Unknown function: abracadabra", result.message());
    }
}
