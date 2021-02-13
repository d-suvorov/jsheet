package org.jsheet.model;

import java.util.Objects;
import java.util.function.BiFunction;

/**
 * Represent a computation result which is either
 * a result {@code value} or an error message {@code message}.
 */
public class Result {
    // TODO consider rewriting this on generics
    private final double value;
    private final String message;
    private final boolean isPresent;

    private Result(double value, String message, boolean isPresent) {
        this.value = value;
        this.message = message;
        this.isPresent = isPresent;
    }

    public boolean isPresent() {
        return isPresent;
    }

    public double get() {
        if (isPresent())
            return value;
        throw new IllegalStateException();
    }

    public String message() {
        if (!isPresent())
            return message;
        throw new IllegalStateException();
    }

    public static Result success(double value) {
        return new Result(value, null, true);
    }

    public static Result failure(String message) {
        return new Result(Double.NaN, message, false);
    }

    public static Result compose(Result a, Result b,
        BiFunction<Double, Double, Double> f)
    {
        if (!a.isPresent()) return a;
        if (!b.isPresent()) return b;
        return Result.success(f.apply(a.get(), b.get()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Result result = (Result) o;

        if (Double.compare(result.value, value) != 0) return false;
        if (isPresent != result.isPresent) return false;
        return Objects.equals(message, result.message);
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(value);
        result = (int) (temp ^ (temp >>> 32));
        result = 31 * result + (message != null ? message.hashCode() : 0);
        result = 31 * result + (isPresent ? 1 : 0);
        return result;
    }
}
