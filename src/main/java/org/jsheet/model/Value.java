package org.jsheet.model;

import org.jsheet.parser.ParseException;
import org.jsheet.parser.ParserUtils;

import java.util.Objects;

public class Value {
    private final boolean isPresent;
    private final Type tag;
    private final Object value;
    private final String message;

    private Value(Type tag, Object value) {
        this.isPresent = true;
        this.tag = tag;
        this.value = value;
        this.message = null;
    }

    private Value(String message) {
        this.isPresent = false;
        this.tag = null;
        this.value = null;
        this.message = message;
    }

    private void assertPresent() {
        if (!isPresent())
            throw new IllegalStateException("empty value");
    }

    public boolean isPresent() {
        return isPresent;
    }

    public Type getTag() {
        assertPresent();
        return tag;
    }

    public Boolean getAsBoolean() {
        assertPresent();
        return (Boolean) value;
    }

    public Double getAsDouble() {
        assertPresent();
        return (Double) value;
    }

    public String getAsString() {
        assertPresent();
        return (String) value;
    }

    public Formula getAsFormula() {
        assertPresent();
        return (Formula) value;
    }

    public RangeValue getAsRange() {
        assertPresent();
        return (RangeValue) value;
    }

    public String getMessage() {
        return message;
    }

    public Object getPlain() {
        return value;
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

    public static Value error(String message) {
        return new Value(message);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Value value1 = (Value) o;

        if (isPresent != value1.isPresent) return false;
        if (tag != value1.tag) return false;
        if (!Objects.equals(value, value1.value)) return false;
        return Objects.equals(message, value1.message);
    }

    @Override
    public int hashCode() {
        int result = (isPresent ? 1 : 0);
        result = 31 * result + (tag != null ? tag.hashCode() : 0);
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + (message != null ? message.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        if (!isPresent())
            return String.format("Error{message=%s}", message);
        else
            return String.format("Success{tag=%s, value=%s}", tag, value);
    }
}
