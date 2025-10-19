package com.dortegau.jq4java.ast;

import com.dortegau.jq4java.json.JqValue;
import java.util.stream.Stream;

/**
 * Implementation of the type function.
 * Returns the type of the input value as a string.
 */
public class Type implements Expression {
  static {
    BuiltinRegistry.register("type", 0);
  }

  @Override
  public Stream<JqValue> evaluate(JqValue input) {
    return Stream.of(input.type());
  }
}
