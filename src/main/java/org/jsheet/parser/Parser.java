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
        return expr(true);
    }

    private Expr expr(boolean read) throws ParseException {
        Expr expr = term(read);
        while (current == PLUS || current == MINUS) {
            String op = current == PLUS ? "+" : "-";
            Expr term = term(false);
            expr = new Binop(op, expr, term);
        }
        return expr;
    }

    private Expr term(boolean read) throws ParseException  {
        Expr term = factor(read);
        while (current == MUL || current == DIV) {
            String op = current == MUL ? "*" : "/";
            Expr factor = factor(false);
            term = new Binop(op, term, factor);
        }
        return term;
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
                Expr expr = expr(false);
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
            args.add(expr(true));
            if (current != COMMA)
                break;
            readNextToken();
        }
        if (current != RPAREN)
            throw new ParseException();
        readNextToken();
        return new Function(name, args);
    }

    private Expr reference(String name) throws ParseException {
        Ref ref = new Ref(name);
        refs.add(ref);
        return ref;
    }

    private Conditional conditional() throws ParseException {
        Expr condition = expr(false);
        if (current != THEN)
            throw new ParseException();
        Expr thenClause = expr(false);
        if (current != ELSE)
            throw new ParseException();
        Expr elseClause = expr(false);
        return new Conditional(condition, thenClause, elseClause);
    }

    private void readNextToken() throws ParseException {
        current = lexer.next();
    }
}
