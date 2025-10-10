package com.dortegau.jq4java.ast;

import com.dortegau.jq4java.json.JqValue;
import java.util.stream.Stream;

public class ArrayIndexing implements Expression {
    private final int index;
    private final Expression input;

    public ArrayIndexing(int index, Expression input) {
        this.index = index;
        this.input = input;
    }

    @Override
    public Stream<JqValue> evaluate(JqValue value) {
        return input.evaluate(value)
            .map(v -> v.getIndex(index));
    }
}
