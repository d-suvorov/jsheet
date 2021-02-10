package org.jsheet.parser;

import org.jsheet.ExpressionBaseVisitor;
import org.jsheet.ExpressionParser;
import org.jsheet.expr.*;

import java.util.List;
import java.util.stream.Collectors;

public class AbstractTreeBuilder extends ExpressionBaseVisitor<Expr> {
    @Override
    public Ref visitRef(ExpressionParser.RefContext ctx) {
        String id = ctx.ID().getText();
        return new Ref(id);
    }

    @Override
    public Const visitConst(ExpressionParser.ConstContext ctx) {
        double value = Double.parseDouble(ctx.NUM().getText());
        return new Const(value);
    }

    @Override
    public Binop visitInfix(ExpressionParser.InfixContext ctx) {
        String op = ctx.op.getText();
        Expr lhs = visit(ctx.left);
        Expr rhs = visit(ctx.right);
        return new Binop(op, lhs, rhs);
    }

    @Override
    public Expr visitParenthesis(ExpressionParser.ParenthesisContext ctx) {
        return visit(ctx.getChild(1));
    }

    @Override
    public Expr visitFunction(ExpressionParser.FunctionContext ctx) {
        List<Expr> args = ctx.args().expr().stream()
            .map(this::visit)
            .collect(Collectors.toList());
        String name = ctx.ID().getText();
        return new Function(name, args);
    }
}
