package org.jsheet;

import org.jsheet.model.expr.*;
import org.jsheet.model.ExprWrapper;
import org.jsheet.model.JSheetTableModel;
import org.jsheet.model.Value;
import org.jsheet.parser.ParserUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

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

    private Literal lit(boolean b) {
        return new Literal(Value.of(b));
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
        String s = "";
        testParserImpl("= \"" + s + "\"", new Literal(Value.of(s)));
        s = "abacaba";
        testParserImpl("= \"" + s + "\"", new Literal(Value.of(s)));
        s = "abra\\\"cadabra";
        testParserImpl("= \"" + s + "\"", new Literal(Value.of("abra\"cadabra")));
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
    public void functionWithNoArgs() {
        Expr expected = new Function("rand", Collections.emptyList());
        testParserImpl("= rand()", expected);
    }

    @Test
    public void functionWithOneArgs() {
        Expr expected = new Function("sqrt", Collections.singletonList(lit(4)));
        testParserImpl("= sqrt(4)", expected);
    }

    @Test
    public void functionWithTwoArgs() {
        Expr expected = new Function(
            "pow", Arrays.asList(lit(2), lit(4))
        );
        testParserImpl("= pow(2, 4)", expected);
    }

    @Test
    public void range() {
        Expr expected = new Function(
            "sum",
            Collections.singletonList(new Range(new Ref("A1"), new Ref("A10")))
        );
        testParserImpl("= sum(A1:A10)", expected);
    }

    @Test
    public void conditional() {
        Expr expected = new Conditional(lit(true), lit(42), lit(43));
        testParserImpl("= if true then 42 else 43", expected);
    }
}
