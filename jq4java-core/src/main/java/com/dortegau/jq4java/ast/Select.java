package com.dortegau.jq4java.ast;

import com.dortegau.jq4java.json.JqValue;

import java.util.stream.Stream;

public class Select implements Expression {
    private final Expression condition;

    static {
        BuiltinRegistry.register("select", 1);
    }

    public Select(Expression condition) {
        this.condition = condition;
    }

    @Override
    public Stream<JqValue> evaluate(JqValue input) {
        return condition.evaluate(input)
                .flatMap(conditionResult -> {
                    if (conditionResult.isTruthy()) {
                        return Stream.of(input);
                    } else {
                        return Stream.empty();
                    }
                });
    }
}