package com.dortegau.jq4java.ast;

import com.dortegau.jq4java.json.JqValue;
import java.util.stream.Stream;

/**
 * Implementation of the flatten function.
 * Flattens arrays by one level of nesting.
 */
public class Flatten implements Expression {
  static {
    BuiltinRegistry.register("flatten", 0);
  }

  @Override
  public Stream<JqValue> evaluate(JqValue input) {
    if (!input.isArray()) {
      throw new RuntimeException("Cannot flatten non-array type: " + input.type());
    }

    return Stream.of(input.flatten(1));
  }
}