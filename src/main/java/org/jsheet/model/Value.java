package org.jsheet.model;

import org.jsheet.parser.ParseException;
import org.jsheet.parser.ParserUtils;

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
        return String.format("Value{tag=%s, value=%s}", tag, value);
    }

    public static Value parse(String strValue) throws ParseException {
        if (strValue.startsWith("=")) {
            Formula formula = ParserUtils.parse(strValue);
            return Value.of(formula);
        } else {
            return parseLiteral(strValue);
        }
    }

    private static Value parseLiteral(String strValue) {
        // Boolean
        if (strValue.equals("false")) return Value.of(false);
        if (strValue.equals("true")) return Value.of(true);

        // Number
        try {
            return Value.of(Double.parseDouble(strValue));
        } catch (NumberFormatException ignored) {}

        // String
        return Value.of(strValue);
    }
}
