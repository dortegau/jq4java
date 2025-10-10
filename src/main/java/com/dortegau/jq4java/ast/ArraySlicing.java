package com.dortegau.jq4java.ast;

import com.dortegau.jq4java.json.JqValue;
import java.util.stream.Stream;

public class ArraySlicing implements Expression {
    private final Integer start;
    private final Integer end;
    private final Expression input;

    public ArraySlicing(Integer start, Integer end, Expression input) {
        this.start = start;
        this.end = end;
        this.input = input;
    }

    @Override
    public Stream<JqValue> evaluate(JqValue value) {
        return input.evaluate(value)
            .map(v -> v.slice(start, end));
    }
}
