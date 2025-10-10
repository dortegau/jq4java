package com.dortegau.jq4java.ast;

import com.dortegau.jq4java.json.JqValue;
import java.util.stream.Stream;

/**
 * Literal expression.
 */
public class Literal implements Expression {
  private final String value;

  public Literal(String value) {
    this.value = value;
  }

  @Override
  public Stream<JqValue> evaluate(JqValue input) {
    return Stream.of(JqValue.literal(value));
  }
}