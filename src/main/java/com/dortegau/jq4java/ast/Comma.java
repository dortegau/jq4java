package com.dortegau.jq4java.ast;

import com.dortegau.jq4java.json.JqValue;
import java.util.List;
import java.util.stream.Stream;

public class Comma implements Expression {
    private final List<Expression> expressions;

    public Comma(List<Expression> expressions) {
        this.expressions = expressions;
    }

    @Override
    public Stream<JqValue> evaluate(JqValue value) {
        return expressions.stream()
            .flatMap(expr -> expr.evaluate(value));
    }
}
