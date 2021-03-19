package org.jsheet.expression;

import java.util.List;
import java.util.stream.Stream;

public class RangeCollector extends ExpressionCollector<Range> {
    @Override
    public Stream<Range> visit(Range range) {
        return Stream.of(range);
    }

    public static List<Range> getRanges(Expression expression) {
        return new RangeCollector().collect(expression);
    }
}
