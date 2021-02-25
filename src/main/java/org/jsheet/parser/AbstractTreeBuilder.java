package org.jsheet.parser;

import org.jsheet.ExpressionBaseVisitor;
import org.jsheet.ExpressionParser;
import org.jsheet.model.Value;
import org.jsheet.model.expr.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AbstractTreeBuilder extends ExpressionBaseVisitor<Expr> {
    private final List<Ref> refs = new ArrayList<>();
    private final List<Range> ranges = new ArrayList<>();

    public List<Ref> getRefs() {
        return refs;
    }

    public List<Range> getRanges() {
        return ranges;
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
        return visitReference(ctx.reference());
    }

    @Override
    public Expr visitRangeExpr(ExpressionParser.RangeExprContext ctx) {
        Ref first = visitReference(ctx.first);
        Ref last = visitReference(ctx.last);
        Range range = new Range(first, last);
        ranges.add(range);
        return range;
    }

    @Override
    public Ref visitReference(ExpressionParser.ReferenceContext ctx) {
        String id = ctx.ID().getText();
        Ref ref = new Ref(id);
        refs.add(ref);
        return ref;
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
