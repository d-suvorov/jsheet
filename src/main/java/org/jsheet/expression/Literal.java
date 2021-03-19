package org.jsheet.expression;

import java.util.stream.Stream;

public abstract class Literal extends Expression {
    @Override
    public Stream<Range> getRanges() {
        return Stream.empty();
    }
}
