package com.dortegau.jq4java.ast;

import com.dortegau.jq4java.json.JqValue;
import java.util.stream.Stream;

/**
 * Implementation of the logical 'not' operator.
 * Returns the negation of the truthiness of the input value.
 */
public class Not implements Expression {
  @Override
  public Stream<JqValue> evaluate(JqValue input) {
    return Stream.of(JqValue.fromBoolean(!input.isTruthy()));
  }
}
