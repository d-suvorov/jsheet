package org.jsheet.parser;

import org.jsheet.expression.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.jsheet.parser.Lexer.Token.*;

public class Parser {
    private final Lexer lexer;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
    }

    public Expression parse() throws ParseException {
        lexer.next();
        if (lexer.current() == END)
            throw new ParseException();
        Expression expression = or();
        if (lexer.current() != END)
            throw new ParseException();
        return expression;
    }

    private Expression or() throws ParseException {
        Expression expr = and();
        while (lexer.current() == OR) {
            String op = lexer.current().binop();
            lexer.next();
            Expression term = and();
            expr = new Binop(op, expr, term);
        }
        return expr;
    }

    private Expression and() throws ParseException {
        Expression expr = comparison();
        while (lexer.current() == AND) {
            String op = lexer.current().binop();
            lexer.next();
            Expression term = comparison();
            expr = new Binop(op, expr, term);
        }
        return expr;
    }

    private Expression comparison() throws ParseException {
        Expression expr = sum();
        while (lexer.current() == EQ || lexer.current() == NE || lexer.current() == LT
            || lexer.current() == LE || lexer.current() == GT || lexer.current() == GE)
        {
            String op = lexer.current().binop();
            lexer.next();
            Expression term = sum();
            expr = new Binop(op, expr, term);
        }
        return expr;
    }

    private Expression sum() throws ParseException {
        Expression expr = product();
        while (lexer.current() == PLUS || lexer.current() == MINUS) {
            String op = lexer.current().binop();
            lexer.next();
            Expression term = product();
            expr = new Binop(op, expr, term);
        }
        return expr;
    }

    private Expression product() throws ParseException  {
        Expression expr = factor();
        while (lexer.current() == MUL || lexer.current() == DIV) {
            String op = lexer.current().binop();
            lexer.next();
            Expression term = factor();
            expr = new Binop(op, expr, term);
        }
        return expr;
    }

    private Expression factor() throws ParseException {
        switch (lexer.current()) {
            case BOOL: return literal(lexer.currentBool());
            case NUM: return literal(lexer.currentNum());
            case MINUS: {
                lexer.next();
                if (lexer.current() != NUM) throw new ParseException();
                return literal(-lexer.currentNum());
            }
            case STR: return literal(lexer.currentString());
            case ID: {
                String name = lexer.currentId();
                lexer.next();
                if (lexer.current() == COLON) {
                    return range(name);
                } else if (lexer.current() == LPAREN) {
                    return function(name);
                } else {
                    return new Reference(name);
                }
            }
            case IF: return conditional();
            case LPAREN: {
                lexer.next();
                Expression expr = or();
                lexer.match(RPAREN);
                return expr;
            }
        }
        throw new ParseException();
    }

    private BooleanLiteral literal(boolean value) throws ParseException {
        lexer.next();
        return new BooleanLiteral(value);
    }

    private DoubleLiteral literal(double value) throws ParseException {
        lexer.next();
        return new DoubleLiteral(value);
    }

    private StringLiteral literal(String value) throws ParseException {
        lexer.next();
        return new StringLiteral(value);
    }

    private Expression range(String firstName) throws ParseException {
        lexer.next();
        if (lexer.current() != ID)
            throw new ParseException();
        Reference first = new Reference(firstName);
        Reference last = new Reference(lexer.currentId());
        Range range = new Range(first, last);
        lexer.next();
        return range;
    }

    private Expression function(String name) throws ParseException {
        lexer.next();
        if (lexer.current() == RPAREN) {
            lexer.next();
            return new Function(name, Collections.emptyList());
        }
        List<Expression> args = new ArrayList<>();
        while (true) {
            args.add(or());
            if (lexer.current() != COMMA)
                break;
            lexer.next();
        }
        lexer.match(RPAREN);
        return new Function(name, args);
    }

    private Conditional conditional() throws ParseException {
        lexer.next();
        Expression condition = or();
        lexer.match(THEN);
        Expression thenClause = or();
        lexer.match(ELSE);
        Expression elseClause = or();
        return new Conditional(condition, thenClause, elseClause);
    }
}
