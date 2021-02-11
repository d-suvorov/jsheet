package org.jsheet.parser;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.jsheet.ExpressionLexer;
import org.jsheet.ExpressionParser;
import org.jsheet.expr.Expr;
import org.jsheet.model.ExprWrapper;

public class ParserUtils {
    // TODO reuse these instances somehow
    public static ExprWrapper parse(String definition) {
        String formula = definition.substring(1); // truncate '='
        ExpressionLexer lexer = new ExpressionLexer(new ANTLRInputStream(formula));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        ExpressionParser parser = new ExpressionParser(tokens);
        ExpressionParser.ExprContext tree = parser.expr();
        AbstractTreeBuilder treeBuilder = new AbstractTreeBuilder();
        Expr expr = treeBuilder.visit(tree);
        return new ExprWrapper(definition, expr, treeBuilder.getRefs());
    }
}
