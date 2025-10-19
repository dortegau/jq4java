package com.dortegau.jq4java.ast;

import com.dortegau.jq4java.json.JqValue;
import java.util.stream.Stream;

/**
 * Implementation of the unique function.
 * Returns an array with duplicate values removed, preserving order.
 */
public class Unique implements Expression {
  static {
    BuiltinRegistry.register("unique", 0);
  }

  @Override
  public Stream<JqValue> evaluate(JqValue input) {
    if (!input.isArray()) {
      throw new RuntimeException("Cannot get unique elements from non-array type: " + input.type());
    }

    return Stream.of(input.unique());
  }
}