package org.jsheet;

import org.jsheet.expr.Binop;
import org.jsheet.expr.Expr;
import org.jsheet.expr.Function;
import org.jsheet.expr.Literal;
import org.jsheet.model.ExprWrapper;
import org.jsheet.model.JSheetTableModel;
import org.jsheet.model.Value;
import org.jsheet.parser.ParserUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ParserTest {
    public static JSheetTableModel model;

    @BeforeAll
    static void setUp() {
        model = new JSheetTableModel();
    }

    private void testParserImpl(String formula, Expr expected) {
        ExprWrapper expr = ParserUtils.parse(formula);
        assertEquals(expected, expr.expression);
    }

    private Literal lit(double v) {
        return new Literal(Value.of(v));
    }

    @Test
    public void booleanLiteral() {
        testParserImpl("= false", new Literal(Value.of(false)));
        testParserImpl("= true", new Literal(Value.of(true)));
    }

    @Test
    public void numberLiteral() {
        testParserImpl("= 42", lit(42));
        testParserImpl("= 42.1", lit(42.1));
        testParserImpl("= .1", lit(.1));
    }

    @Test
    public void stringLiteral() {
        String s = "abra\\\"cadabra";
        testParserImpl("= \"" + s + "\"", new Literal(Value.of(s)));
    }

    @Test
    public void simpleExpression1() {
        Expr expected = new Binop(
            "+",
            lit(1.),
            new Binop("*", lit(2.), lit(3.))
        );
        testParserImpl("= 1 + 2 * 3", expected);
    }

    @Test
    public void simpleExpression2() {
        Expr expected = new Binop(
            "*",
            new Binop("+", lit(1.), lit(2.)),
            lit(3.)
        );
        testParserImpl("= (1 + 2) * 3", expected);
    }

    @Test
    public void simpleExpression3() {
        Expr expected = new Function(
            "pow",
            Arrays.asList(lit(2), lit(4))
        );
        testParserImpl("= pow(2, 4)", expected);
    }
}
