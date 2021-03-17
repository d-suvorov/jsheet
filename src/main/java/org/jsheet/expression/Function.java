package org.jsheet.expression;

import org.jsheet.evaluation.EvaluationException;
import org.jsheet.evaluation.EvaluationVisitor;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Function extends Expression {
    private final String name;
    private final List<Expression> args;

    public Function(String name, List<Expression> args) {
        this.name = name;
        this.args = args;
    }

    @Override
    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public <R> R evaluate(EvaluationVisitor<R> visitor) throws EvaluationException {
        return visitor.visit(this);
    }

    @Override
    public Stream<Reference> getReferences() {
        return args.stream().flatMap(Expression::getReferences);
    }

    @Override
    public Stream<Range> getRanges() {
        return args.stream().flatMap(Expression::getRanges);
    }

    public String getName() {
        return name;
    }

    public List<Expression> getArgs() {
        return args;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Function function = (Function) o;

        if (!Objects.equals(name, function.name)) return false;
        return Objects.equals(args, function.args);
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (args != null ? args.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        String argsStr = args.stream()
            .map(Expression::toString)
            .collect(Collectors.joining(", "));
        return String.format("%s(%s)", name, argsStr);
    }
}
