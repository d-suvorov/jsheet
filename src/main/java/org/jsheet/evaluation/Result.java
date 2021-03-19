package org.jsheet.evaluation;

import java.util.Objects;
import java.util.function.BinaryOperator;
import java.util.function.Function;

/**
 * Represent a computation result which is either
 * a result {@code value} or an error message {@code message}.
 */
public class Result {
    private final Value value;
    private final String message;
    private final boolean isPresent;

    private Result(Value value, String message, boolean isPresent) {
        this.value = value;
        this.message = message;
        this.isPresent = isPresent;
    }

    public boolean isPresent() {
        return isPresent;
    }

    public Value get() {
        if (isPresent())
            return value;
        throw new IllegalStateException();
    }

    public String message() {
        if (!isPresent())
            return message;
        throw new IllegalStateException();
    }

    public Result map(Function<Value, Value> f) {
        if (!isPresent())
            return this;
        return Result.success(f.apply(value));
    }

    public Result flatMap(Function<Value, Result> f) {
        if (!isPresent())
            return this;
        return f.apply(value);
    }

    public Result typecheck(Type expected) {
        if (!isPresent())
            return this;
        if (value.getTag() == expected)
            return this;
        return Result.failure(typeMismatchMessage(expected, value.getTag()));
    }

    private static String typeMismatchMessage(Type expected, Type actual) {
        return String.format("Expected %s and got %s", expected.name(), actual.name());
    }

    public static Result success(Value value) {
        return new Result(value, null, true);
    }

    public static Result failure(String message) {
        return new Result(null, message, false);
    }

    public static Result combine(Result a, Result b, BinaryOperator<Value> combiner) {
        return a.flatMap(av ->
            b.flatMap(bv ->
                Result.success(combiner.apply(av, bv))
            )
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Result result = (Result) o;

        if (isPresent != result.isPresent) return false;
        if (!Objects.equals(value, result.value)) return false;
        return Objects.equals(message, result.message);
    }

    @Override
    public int hashCode() {
        int result = value != null ? value.hashCode() : 0;
        result = 31 * result + (message != null ? message.hashCode() : 0);
        result = 31 * result + (isPresent ? 1 : 0);
        return result;
    }
}
