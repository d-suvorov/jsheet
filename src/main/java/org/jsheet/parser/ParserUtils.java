package org.jsheet.parser;

import org.jsheet.data.Formula;
import org.jsheet.expression.Expression;
import org.jsheet.evaluation.Value;

public class ParserUtils {
    public static Value parseValue(String strValue) throws ParseException {
        if (strValue.startsWith("=")) {
            Formula formula = parseFormula(strValue);
            return Value.of(formula);
        } else {
            return parseLiteral(strValue);
        }
    }

    public static Formula parseFormula(String definition) throws ParseException {
        String formula = definition.substring(1); // truncate '='
        Lexer lexer = new Lexer(formula);
        Parser parser = new Parser(lexer);
        Expression expr = parser.parse();
        return new Formula(definition, expr);
    }

    private static Value parseLiteral(String strValue) {
        // Boolean
        if (Lexer.BOOL_LITERALS.containsKey(strValue))
            return Value.of(Lexer.BOOL_LITERALS.get(strValue));

        // Number
        try {
            return Value.of(Double.parseDouble(strValue));
        } catch (NumberFormatException ignored) {}

        // String
        return Value.of(strValue);
    }
}
