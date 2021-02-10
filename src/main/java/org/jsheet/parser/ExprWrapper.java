package org.jsheet.parser;

import org.jsheet.expr.Expr;

public class ExprWrapper {
    public final String definition;
    public final Expr expression;

    public ExprWrapper(String definition, Expr expression) {
        this.definition = definition;
        this.expression = expression;
    }

    @Override
    public String toString() {
        return definition;
    }
}
