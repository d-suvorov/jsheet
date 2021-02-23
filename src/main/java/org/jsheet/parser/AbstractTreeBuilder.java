package org.jsheet.parser;

import org.jsheet.ExpressionBaseVisitor;
import org.jsheet.ExpressionParser;
import org.jsheet.model.expr.*;
import org.jsheet.model.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AbstractTreeBuilder extends ExpressionBaseVisitor<Expr> {
    private final List<String> refs = new ArrayList<>();

    public List<String> getRefs() {
        return refs;
    }

    @Override
    public Literal visitBoolean(ExpressionParser.BooleanContext ctx) {
        boolean b = ctx.BOOL().getText().equals("true");
        return new Literal(Value.of(b));
    }

    @Override
    public Literal visitNumber(ExpressionParser.NumberContext ctx) {
        Double d = Double.parseDouble(ctx.NUM().getText());
        return new Literal(Value.of(d));
    }

    @Override
    public Literal visitString(ExpressionParser.StringContext ctx) {
        String s = ctx.STR().getText();
        return new Literal(Value.of(s.substring(1, s.length() - 1)));
    }

    @Override
    public Expr visitLiteralExpr(ExpressionParser.LiteralExprContext ctx) {
        return visit(ctx.literal());
    }

    @Override
    public Ref visitReferenceExpr(ExpressionParser.ReferenceExprContext ctx) {
        String id = ctx.ID().getText();
        refs.add(id);
        return new Ref(id);
    }

    @Override
    public Binop visitInfixExpr(ExpressionParser.InfixExprContext ctx) {
        String op = ctx.op.getText();
        Expr lhs = visit(ctx.left);
        Expr rhs = visit(ctx.right);
        return new Binop(op, lhs, rhs);
    }

    @Override
    public Expr visitParenthesisExpr(ExpressionParser.ParenthesisExprContext ctx) {
        return visit(ctx.getChild(1));
    }

    @Override
    public Expr visitFunctionExpr(ExpressionParser.FunctionExprContext ctx) {
        List<Expr> args = ctx.args().expr().stream()
            .map(this::visit)
            .collect(Collectors.toList());
        String name = ctx.ID().getText();
        return new Function(name, args);
    }

    @Override
    public Expr visitConditionalExpr(ExpressionParser.ConditionalExprContext ctx) {
        Expr condition = visit(ctx.cond);
        Expr thenClause = visit(ctx.thenClause);
        Expr elseClause = visit(ctx.elseClause);
        return new Conditional(condition, thenClause, elseClause);
    }
}
