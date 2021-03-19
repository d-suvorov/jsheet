package org.jsheet.parser;

import java.util.Map;

public class Lexer {
    public enum Token {
        MUL, DIV, PLUS, MINUS,
        LT, LE, GT, GE, EQ, NE,
        AND, OR, LPAREN, RPAREN,
        COMMA, COLON, IF, THEN, ELSE,
        ID, BOOL, NUM, STR, END;

        public String binop() {
            switch (this) {
                case MUL:   return "*";
                case DIV:   return "/";
                case PLUS:  return "+";
                case MINUS: return "-";
                case LT:    return "<";
                case LE:    return "<=";
                case GT:    return ">";
                case GE:    return ">=";
                case EQ:    return "==";
                case NE:    return "!=";
                case AND:   return "&&";
                case OR:    return "||";
                default:    throw new IllegalStateException("not a binop");
            }
        }
    }

    public static final Map<String, Token> KEYWORDS = Map.of(
        "if", Token.IF,
        "then", Token.THEN,
        "else", Token.ELSE
    );

    public static final Map<String, Boolean> BOOL_LITERALS = Map.of(
        "false", false,
        "true", true
    );

    private final CharSequence input;
    private int position;

    private String currentId;
    private boolean currentBool;
    private double currentNum;
    private String currentString;

    public Lexer(CharSequence input) {
        this.input = input;
        this.position = 0;
    }

    public String currentId() {
        return currentId;
    }

    public boolean currentBool() {
        return currentBool;
    }

    public double currentNum() {
        return currentNum;
    }

    public String currentString() {
        return currentString;
    }

    public Token next() throws ParseException {
        char c;
        do {
            if (!hasNextChar())
                return Token.END;
            c = nextChar();
        } while (Character.isWhitespace(c));

        switch (c) {
            case '*': return Token.MUL;
            case '/': return Token.DIV;
            case '+': return Token.PLUS;
            case '-': return Token.MINUS;
            case '<': {
                if (hasNextChar() && peek() == '=') {
                    nextChar();
                    return Token.LE;
                }
                return Token.LT;
            }
            case '>': {
                if (hasNextChar() && peek() == '=') {
                    nextChar();
                    return Token.GE;
                }
                return Token.GT;
            }
            case '=': {
                if (hasNextChar() && peek() == '=') {
                    nextChar();
                    return Token.EQ;
                }
                throw new ParseException();
            }
            case '!': {
                if (hasNextChar() && peek() == '=') {
                    nextChar();
                    return Token.NE;
                }
                throw new ParseException();
            }
            case '&': {
                if (hasNextChar() && peek() == '&') {
                    nextChar();
                    return Token.AND;
                }
                throw new ParseException();
            }
            case '|': {
                if (hasNextChar() && peek() == '|') {
                    nextChar();
                    return Token.OR;
                }
                throw new ParseException();
            }
            case '(': return Token.LPAREN;
            case ')': return Token.RPAREN;
            case ',': return Token.COMMA;
            case ':': return Token.COLON;
            case '\"': {
                StringBuilder sb = new StringBuilder();
                while (hasNextChar() && peek() != '\"') {
                    char c1 = nextChar();
                    if (c1 != '\\') {
                        sb.append(c1);
                        continue;
                    }
                    // Escape sequence
                    if (!hasNextChar())
                        throw new ParseException();
                    sb.append(nextChar());
                }
                if (!hasNextChar())
                    throw new ParseException();
                nextChar();
                currentString = sb.toString();
                return Token.STR;
            }
        }

        if (Character.isDigit(c) || c == '.') {
            StringBuilder sb = new StringBuilder();
            sb.append(c);
            if (Character.isDigit(c)) {
                // Match whole part and the dot
                while (hasNextChar() && Character.isDigit(peek())) {
                    sb.append(nextChar());
                }
                if (hasNextChar() && peek() == '.') {
                    sb.append(nextChar());
                }
            }
            // Match decimal part
            while (hasNextChar() && Character.isDigit(peek())) {
                sb.append(nextChar());
            }
            currentNum = Double.parseDouble(sb.toString());
            return Token.NUM;
        }

        if (isValidIdStartCharacter(c)) {
            StringBuilder sb = new StringBuilder();
            sb.append(c);
            while (hasNextChar() && isValidIdCharacter(peek())) {
                sb.append(nextChar());
            }
            String name = sb.toString();
            if (KEYWORDS.containsKey(name)) {
                return KEYWORDS.get(name);
            }
            if (BOOL_LITERALS.containsKey(name)) {
                currentBool = BOOL_LITERALS.get(name);
                return Token.BOOL;
            }
            currentId = name;
            return Token.ID;
        }

        throw new ParseException();
    }

    private boolean isValidIdStartCharacter(char c) {
        return Character.isLetter(c) || c == '$';
    }

    private boolean isValidIdCharacter(char c) {
        return Character.isLetterOrDigit(c) || c == '$';
    }

    private boolean hasNextChar() {
        return position < input.length();
    }

    private char nextChar() {
        return input.charAt(position++);
    }

    private char peek() {
        return input.charAt(position);
    }
}
