package org.jsheet.expression;

import java.util.List;
import java.util.stream.Stream;

public class ReferenceCollector extends ExpressionCollector<Reference> {
    @Override
    public Stream<Reference> visit(Reference reference) {
        return Stream.of(reference);
    }

    public static List<Reference> getReferences(Expression expression) {
        return new ReferenceCollector().collect(expression);
    }
}
