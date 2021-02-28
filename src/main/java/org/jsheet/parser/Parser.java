package org.jsheet.parser;

import org.jsheet.model.Value;
import org.jsheet.model.expression.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.jsheet.parser.Lexer.Token.*;

public class Parser {
    private final Lexer lexer;
    private Lexer.Token current;

    private final List<Reference> refs = new ArrayList<>();
    private final List<Range> ranges = new ArrayList<>();

    public Parser(Lexer lexer) {
        this.lexer = lexer;
    }

    public List<Reference> getRefs() {
        return refs;
    }

    public List<Range> getRanges() {
        return ranges;
    }

    public Expression parse() throws ParseException {
        readNextToken();
        if (current == END)
            throw new ParseException();
        return or(true);
    }

    private Expression or(boolean read) throws ParseException {
        Expression expr = and(read);
        while (current == OR) {
            String op = current.binop();
            Expression term = and(false);
            expr = new Binop(op, expr, term);
        }
        return expr;
    }

    private Expression and(boolean read) throws ParseException {
        Expression expr = comparison(read);
        while (current == AND) {
            String op = current.binop();
            Expression term = comparison(false);
            expr = new Binop(op, expr, term);
        }
        return expr;
    }

    private Expression comparison(boolean read) throws ParseException {
        Expression expr = sum(read);
        while (current == EQ || current == NE || current == LT
            || current == LE || current == GT || current == GE)
        {
            String op = current.binop();
            Expression term = sum(false);
            expr = new Binop(op, expr, term);
        }
        return expr;
    }

    private Expression sum(boolean read) throws ParseException {
        Expression expr = product(read);
        while (current == PLUS || current == MINUS) {
            String op = current.binop();
            Expression term = product(false);
            expr = new Binop(op, expr, term);
        }
        return expr;
    }

    private Expression product(boolean read) throws ParseException  {
        Expression expr = factor(read);
        while (current == MUL || current == DIV) {
            String op = current.binop();
            Expression term = factor(false);
            expr = new Binop(op, expr, term);
        }
        return expr;
    }

    private Expression factor(boolean read) throws ParseException {
        if (!read)
            readNextToken();
        switch (current) {
            case BOOL: return literal(Value.of(lexer.currentBool()));
            case NUM: return literal(Value.of(lexer.currentNum()));
            case STR: return literal(Value.of(lexer.currentString()));
            case ID: {
                String name = lexer.currentId();
                readNextToken();
                if (current == COLON) {
                    return range(name);
                } else if (current == LPAREN) {
                    return function(name);
                } else {
                    return reference(name);
                }
            }
            case IF: return conditional();
            case LPAREN: {
                Expression expr = or(false);
                if (current != RPAREN)
                    throw new ParseException();
                readNextToken();
                return expr;
            }
        }
        throw new ParseException();
    }

    private Expression literal(Value value) throws ParseException {
        readNextToken();
        return new Literal(value);
    }

    private Expression range(String firstName) throws ParseException {
        readNextToken();
        if (current != ID)
            throw new ParseException();
        Reference first = new Reference(firstName);
        Reference last = new Reference(lexer.currentId());
        Range range = new Range(first, last);
        refs.add(first);
        refs.add(last);
        ranges.add(range);
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
            args.add(or(true));
            if (current != COMMA)
                break;
            readNextToken();
        }
        if (current != RPAREN)
            throw new ParseException();
        readNextToken();
        return new Function(name, args);
    }

    private Expression reference(String name) {
        Reference ref = new Reference(name);
        refs.add(ref);
        return ref;
    }

    private Conditional conditional() throws ParseException {
        Expression condition = or(false);
        if (current != THEN)
            throw new ParseException();
        Expression thenClause = or(false);
        if (current != ELSE)
            throw new ParseException();
        Expression elseClause = or(false);
        return new Conditional(condition, thenClause, elseClause);
    }

    private void readNextToken() throws ParseException {
        current = lexer.next();
    }
}
