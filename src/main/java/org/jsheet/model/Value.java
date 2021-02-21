package org.jsheet.model;

import java.util.Objects;

public class Value {
    public enum Type {
        BOOLEAN, DOUBLE, STRING, EXPR
    }

    private final Type tag;
    private final Object value;

    private Value(Type tag, Object value) {
        this.tag = tag;
        this.value = value;
    }

    public Type getTag() {
        return tag;
    }

    public Boolean getAsBoolean() {
        return (Boolean) value;
    }

    public Double getAsDouble() {
        return (Double) value;
    }

    public String getAsString() {
        return (String) value;
    }

    public ExprWrapper getAsExpr() {
        return (ExprWrapper) value;
    }

    public static Value of(Boolean b) {
        return new Value(Type.BOOLEAN, b);
    }

    public static Value of(Double d) {
        return new Value(Type.DOUBLE, d);
    }

    public static Value of(String s) {
        return new Value(Type.STRING, s);
    }

    public static Value of(ExprWrapper e) {
        return new Value(Type.EXPR, e);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Value value1 = (Value) o;

        if (tag != value1.tag) return false;
        return Objects.equals(value, value1.value);
    }

    @Override
    public int hashCode() {
        int result = tag != null ? tag.hashCode() : 0;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

    @Override
    // Used when user edits cell content
    // TODO try to implement it with table editor
    public String toString() {
        switch (getTag()) {
            case BOOLEAN: return String.valueOf(getAsBoolean());
            case DOUBLE: return String.valueOf(getAsDouble());
            case STRING: return getAsString();
            case EXPR: return getAsExpr().originalDefinition;
        }
        throw new AssertionError();
    }
}
