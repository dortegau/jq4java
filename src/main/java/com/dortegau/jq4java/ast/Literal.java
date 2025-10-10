package com.dortegau.jq4java.ast;

import com.dortegau.jq4java.json.JqValue;
import com.dortegau.jq4java.json.OrgJsonValue;
import java.util.stream.Stream;

public class Literal implements Expression {
    private final String value;

    public Literal(String value) {
        this.value = value;
    }

    @Override
    public Stream<JqValue> evaluate(JqValue input) {
        return Stream.of(OrgJsonValue.parse(value));
    }
}
