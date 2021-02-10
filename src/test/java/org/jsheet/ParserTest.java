package org.jsheet;

import org.jsheet.expr.Expr;
import org.jsheet.parser.ParserUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ParserTest {
    @Test
    public void simpleExpression1() {
        Expr expr = ParserUtils.parse("1 + 2 * 3");
        assertEquals(1 + 2 * 3, expr.eval(), 0);
    }

    @Test
    public void simpleExpression2() {
        Expr expr = ParserUtils.parse("(1 + 2) * 3");
        assertEquals((1 + 2) * 3, expr.eval(), 0);
    }

    @Test
    public void simpleExpression3() {
        Expr expr = ParserUtils.parse("pow(2, 4)");
        assertEquals(Math.pow(2, 4), expr.eval(), 0);
    }

    @Test
    public void simpleExpression4() {
        Expr expr = ParserUtils.parse("abracadabra(2, 4)");
        assertThrows(AssertionError.class, expr::eval);
    }
}
