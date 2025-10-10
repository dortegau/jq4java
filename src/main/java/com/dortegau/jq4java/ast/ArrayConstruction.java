package com.dortegau.jq4java.ast;

import com.dortegau.jq4java.json.JqValue;
import java.util.List;
import java.util.stream.Stream;

public class ArrayConstruction implements Expression {
    private final List<Expression> elements;

    public ArrayConstruction(List<Expression> elements) {
        this.elements = elements;
    }

    @Override
    public Stream<JqValue> evaluate(JqValue value) {
        JqValue[] results = new JqValue[elements.size()];
        for (int i = 0; i < elements.size(); i++) {
            results[i] = elements.get(i).evaluate(value).findFirst().orElse(null);
        }
        return Stream.of(JqValue.array(results));
    }
}
