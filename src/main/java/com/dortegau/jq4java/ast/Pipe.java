package com.dortegau.jq4java.ast;

import com.dortegau.jq4java.json.JqValue;
import java.util.stream.Stream;

public class Pipe implements Expression {
    private final Expression left;
    private final Expression right;

    public Pipe(Expression left, Expression right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public Stream<JqValue> evaluate(JqValue input) {
        return left.evaluate(input)
            .flatMap(leftResult -> right.evaluate(leftResult));
    }
}
