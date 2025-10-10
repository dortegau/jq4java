package com.dortegau.jq4java.ast;

import com.dortegau.jq4java.json.JqValue;
import java.util.stream.Stream;

public interface Expression {
    Stream<JqValue> evaluate(JqValue input);
}
