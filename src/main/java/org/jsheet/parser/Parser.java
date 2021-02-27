package org.jsheet.parser;

import org.jsheet.model.Value;
import org.jsheet.model.expr.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.jsheet.parser.Lexer.Token.*;

public class Parser {
    private final Lexer lexer;
    private Lexer.Token current;

    private final List<Ref> refs = new ArrayList<>();
    private final List<Range> ranges = new ArrayList<>();

    public Parser(Lexer lexer) {
        this.lexer = lexer;
    }

    public List<Ref> getRefs() {
        return refs;
    }

    public List<Range> getRanges() {
        return ranges;
    }

    public Expr parse() throws ParseException {
        readNextToken();
        if (current == END)
            throw new ParseException();
        return or(true);
    }

    private Expr or(boolean read) throws ParseException {
        Expr expr = and(read);
        while (current == OR) {
            String op = current.binop();
            Expr term = and(false);
            expr = new Binop(op, expr, term);
        }
        return expr;
    }

    private Expr and(boolean read) throws ParseException {
        Expr expr = comparison(read);
        while (current == AND) {
            String op = current.binop();
            Expr term = comparison(false);
            expr = new Binop(op, expr, term);
        }
        return expr;
    }

    private Expr comparison(boolean read) throws ParseException {
        Expr expr = sum(read);
        while (current == EQ || current == NE || current == LT
            || current == LE || current == GT || current == GE)
        {
            String op = current.binop();
            Expr term = sum(false);
            expr = new Binop(op, expr, term);
        }
        return expr;
    }

    private Expr sum(boolean read) throws ParseException {
        Expr expr = product(read);
        while (current == PLUS || current == MINUS) {
            String op = current.binop();
            Expr term = product(false);
            expr = new Binop(op, expr, term);
        }
        return expr;
    }

    private Expr product(boolean read) throws ParseException  {
        Expr expr = factor(read);
        while (current == MUL || current == DIV) {
            String op = current.binop();
            Expr term = factor(false);
            expr = new Binop(op, expr, term);
        }
        return expr;
    }

    private Expr factor(boolean read) throws ParseException {
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
                Expr expr = or(false);
                if (current != RPAREN)
                    throw new ParseException();
                readNextToken();
                return expr;
            }
        }
        throw new ParseException();
    }

    private Expr literal(Value value) throws ParseException {
        readNextToken();
        return new Literal(value);
    }

    private Expr range(String firstName) throws ParseException {
        readNextToken();
        if (current != ID)
            throw new ParseException();
        Ref first = new Ref(firstName);
        Ref last = new Ref(lexer.currentId());
        Range range = new Range(first, last);
        refs.add(first);
        refs.add(last);
        ranges.add(range);
        readNextToken();
        return range;
    }

    private Expr function(String name) throws ParseException {
        readNextToken();
        if (current == RPAREN) {
            readNextToken();
            return new Function(name, Collections.emptyList());
        }
        List<Expr> args = new ArrayList<>();
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

    private Expr reference(String name) {
        Ref ref = new Ref(name);
        refs.add(ref);
        return ref;
    }

    private Conditional conditional() throws ParseException {
        Expr condition = or(false);
        if (current != THEN)
            throw new ParseException();
        Expr thenClause = or(false);
        if (current != ELSE)
            throw new ParseException();
        Expr elseClause = or(false);
        return new Conditional(condition, thenClause, elseClause);
    }

    private void readNextToken() throws ParseException {
        current = lexer.next();
    }
}
