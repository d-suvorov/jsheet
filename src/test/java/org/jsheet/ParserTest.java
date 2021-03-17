package org.jsheet;

import org.jsheet.expression.*;
import org.jsheet.parser.Lexer;
import org.jsheet.parser.ParseException;
import org.jsheet.parser.Parser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ParserTest {
    private void testParserImpl(String input, Expression expected) {
        try {
            Lexer lexer = new Lexer(input);
            Parser parser = new Parser(lexer);
            assertEquals(expected, parser.parse());
        } catch (ParseException ignored) {
            Assertions.fail("should be correctly parsed");
        }
    }

    private DoubleLiteral lit(double v) {
        return new DoubleLiteral(v);
    }

    @SuppressWarnings("SameParameterValue")
    private BooleanLiteral lit(boolean b) {
        return new BooleanLiteral(b);
    }

    private StringLiteral lit(String s) {
        return new StringLiteral(s);
    }

    private Binop bin(Expression left, String op, Expression right) {
        return new Binop(op, left, right);
    }

    @Test
    public void booleanLiteral() {
        testParserImpl("false", lit(false));
        testParserImpl("true", lit(true));
    }

    @Test
    public void numberLiteral() {
        testParserImpl("42", lit(42));
        testParserImpl("42.1", lit(42.1));
        testParserImpl(".1", lit(.1));
        testParserImpl("-42", lit(-42));
        testParserImpl("-42.1", lit(-42.1));
        testParserImpl("-.1", lit(-.1));
    }

    @Test
    public void stringLiteral() {
        String s = "";
        testParserImpl("\"" + s + "\"", lit(s));
        s = "abacaba";
        testParserImpl("\"" + s + "\"", lit(s));
        s = "abra\\\"cadabra";
        testParserImpl("\"" + s + "\"", lit("abra\"cadabra"));
    }

    @Test
    public void simpleExpression1() {
        Expression expected = bin(
            lit(1),
            "+",
            bin(lit(2), "*", lit(3))
        );
        testParserImpl("1 + 2 * 3", expected);
    }

    @Test
    public void simpleExpression2() {
        Expression expected = bin(
            bin(lit(1), "+", lit(2)),
            "*",
            lit(3)
        );
        testParserImpl("(1 + 2) * 3", expected);
    }

    @Test
    public void priority() {
        Expression expected = bin(
            bin(lit(1), "==", lit(2)),
            "||",
            bin(
                bin(bin(lit(1), "+", lit(1)), "==", bin(lit(1), "+", lit(1))),
                "&&",
                bin(bin(lit(42), "/", lit(2)), ">", lit(20))
            )
        );
        testParserImpl("1 == 2 || 1 + 1 == 1 + 1 && 42 / 2 > 20", expected);
    }

    @Test
    public void functionWithNoArgs() {
        Expression expected = new Function("rand", Collections.emptyList());
        testParserImpl("rand()", expected);
    }

    @Test
    public void functionWithOneArgs() {
        Expression expected = new Function("sqrt", Collections.singletonList(lit(4)));
        testParserImpl("sqrt(4)", expected);
    }

    @Test
    public void functionWithTwoArgs() {
        Expression expected = new Function(
            "pow", List.of(lit(2), lit(4))
        );
        testParserImpl("pow(2, 4)", expected);
    }

    @Test
    public void range() {
        Expression expected = new Function(
            "sum",
            Collections.singletonList(new Range(new Reference("A1"), new Reference("A10")))
        );
        testParserImpl("sum(A1:A10)", expected);
    }

    @Test
    public void conditional() {
        Expression expected = new Conditional(lit(true), lit(42), lit(43));
        testParserImpl("if true then 42 else 43", expected);
    }
}
