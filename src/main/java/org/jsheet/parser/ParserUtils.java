package org.jsheet.parser;

import org.jsheet.model.ExprWrapper;
import org.jsheet.model.expr.Expr;

public class ParserUtils {
    public static ExprWrapper parse(String definition) {
        String formula = definition.substring(1); // truncate '='
        Lexer lexer = new Lexer(formula);
        Parser parser = new Parser(lexer);
        try {
            Expr expr = parser.parse();
            return new ExprWrapper(definition, expr, parser.getRefs(), parser.getRanges());
        } catch (ParseException e) {
            return new ExprWrapper(definition, "Parsing error");
        }
    }
}
