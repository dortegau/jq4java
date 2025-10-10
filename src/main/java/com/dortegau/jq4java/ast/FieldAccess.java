package com.dortegau.jq4java.ast;

import com.dortegau.jq4java.json.JqValue;
import java.util.stream.Stream;

public class FieldAccess implements Expression {
    private final String fieldName;
    private final Expression base;

    public FieldAccess(String fieldName) {
        this(fieldName, new Identity());
    }

    public FieldAccess(String fieldName, Expression base) {
        this.fieldName = fieldName;
        this.base = base;
    }

    @Override
    public Stream<JqValue> evaluate(JqValue input) {
        return base.evaluate(input)
            .map(baseValue -> baseValue.get(fieldName));
    }
}
