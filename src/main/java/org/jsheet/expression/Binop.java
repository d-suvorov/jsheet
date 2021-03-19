package org.jsheet.expression;

import org.jsheet.evaluation.EvaluationException;
import org.jsheet.evaluation.EvaluationVisitor;

import java.util.Objects;

public class Binop extends Expression {
    private final String op;
    private final Expression left;
    private final Expression right;

    public Binop(String op, Expression left, Expression right) {
        this.op = op;
        this.left = left;
        this.right = right;
    }

    @Override
    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public <R> R evaluate(EvaluationVisitor<R> visitor) throws EvaluationException {
        return visitor.visit(this);
    }

    public String getOp() {
        return op;
    }

    public Expression getLeft() {
        return left;
    }

    public Expression getRight() {
        return right;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Binop binop = (Binop) o;

        if (!Objects.equals(op, binop.op)) return false;
        if (!Objects.equals(left, binop.left)) return false;
        return Objects.equals(right, binop.right);
    }

    @Override
    public int hashCode() {
        int result = op != null ? op.hashCode() : 0;
        result = 31 * result + (left != null ? left.hashCode() : 0);
        result = 31 * result + (right != null ? right.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return String.format("(%s %s %s)", left, op, right);
    }
}
