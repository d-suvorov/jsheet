package org.jsheet.parser;

import org.jsheet.expression.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.jsheet.parser.Lexer.Token.*;

public class Parser {
    private final Lexer lexer;
    private Lexer.Token current;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
    }

    public Expression parse() throws ParseException {
        readNextToken();
        if (current == END)
            throw new ParseException();
        return or();
    }

    private Expression or() throws ParseException {
        Expression expr = and();
        while (current == OR) {
            String op = current.binop();
            readNextToken();
            Expression term = and();
            expr = new Binop(op, expr, term);
        }
        return expr;
    }

    private Expression and() throws ParseException {
        Expression expr = comparison();
        while (current == AND) {
            String op = current.binop();
            readNextToken();
            Expression term = comparison();
            expr = new Binop(op, expr, term);
        }
        return expr;
    }

    private Expression comparison() throws ParseException {
        Expression expr = sum();
        while (current == EQ || current == NE || current == LT
            || current == LE || current == GT || current == GE)
        {
            String op = current.binop();
            readNextToken();
            Expression term = sum();
            expr = new Binop(op, expr, term);
        }
        return expr;
    }

    private Expression sum() throws ParseException {
        Expression expr = product();
        while (current == PLUS || current == MINUS) {
            String op = current.binop();
            readNextToken();
            Expression term = product();
            expr = new Binop(op, expr, term);
        }
        return expr;
    }

    private Expression product() throws ParseException  {
        Expression expr = factor();
        while (current == MUL || current == DIV) {
            String op = current.binop();
            readNextToken();
            Expression term = factor();
            expr = new Binop(op, expr, term);
        }
        return expr;
    }

    private Expression factor() throws ParseException {
        switch (current) {
            case BOOL: return literal(lexer.currentBool());
            case NUM: return literal(lexer.currentNum());
            case MINUS: {
                readNextToken();
                if (current != NUM) throw new ParseException();
                return literal(-lexer.currentNum());
            }
            case STR: return literal(lexer.currentString());
            case ID: {
                String name = lexer.currentId();
                readNextToken();
                if (current == COLON) {
                    return range(name);
                } else if (current == LPAREN) {
                    return function(name);
                } else {
                    return new Reference(name);
                }
            }
            case IF: return conditional();
            case LPAREN: {
                readNextToken();
                Expression expr = or();
                if (current != RPAREN)
                    throw new ParseException();
                readNextToken();
                return expr;
            }
        }
        throw new ParseException();
    }

    private BooleanLiteral literal(boolean value) throws ParseException {
        readNextToken();
        return new BooleanLiteral(value);
    }

    private DoubleLiteral literal(double value) throws ParseException {
        readNextToken();
        return new DoubleLiteral(value);
    }

    private StringLiteral literal(String value) throws ParseException {
        readNextToken();
        return new StringLiteral(value);
    }

    private Expression range(String firstName) throws ParseException {
        readNextToken();
        if (current != ID)
            throw new ParseException();
        Reference first = new Reference(firstName);
        Reference last = new Reference(lexer.currentId());
        Range range = new Range(first, last);
        readNextToken();
        return range;
    }

    private Expression function(String name) throws ParseException {
        readNextToken();
        if (current == RPAREN) {
            readNextToken();
            return new Function(name, Collections.emptyList());
        }
        List<Expression> args = new ArrayList<>();
        while (true) {
            args.add(or());
            if (current != COMMA)
                break;
            readNextToken();
        }
        if (current != RPAREN)
            throw new ParseException();
        readNextToken();
        return new Function(name, args);
    }

    private Conditional conditional() throws ParseException {
        readNextToken();
        Expression condition = or();
        if (current != THEN)
            throw new ParseException();
        readNextToken();
        Expression thenClause = or();
        if (current != ELSE)
            throw new ParseException();
        readNextToken();
        Expression elseClause = or();
        return new Conditional(condition, thenClause, elseClause);
    }

    private void readNextToken() throws ParseException {
        current = lexer.next();
    }
}
