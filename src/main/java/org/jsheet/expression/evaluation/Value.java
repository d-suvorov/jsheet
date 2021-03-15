package org.jsheet.expression.evaluation;

import org.jsheet.data.Formula;

import java.util.Objects;

public class Value {
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

    public Formula getAsFormula() {
        return (Formula) value;
    }

    public RangeValue getAsRange() {
        return (RangeValue) value;
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

    public static Value of(Formula e) {
        return new Value(Type.FORMULA, e);
    }

    public static Value of(RangeValue r) {
        return new Value(Type.RANGE, r);
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
    public String toString() {
        switch (getTag()) {
            case BOOLEAN: return getAsBoolean().toString();
            case DOUBLE: return getAsDouble().toString();
            case STRING: return getAsString();
            case FORMULA: return getAsFormula().originalDefinition;
            /* Range values only occur inside formulae */
            case RANGE: throw new AssertionError();
        }
        throw new AssertionError();
    }
}
