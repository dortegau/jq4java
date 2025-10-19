package com.dortegau.jq4java.ast;

import com.dortegau.jq4java.json.JqValue;
import java.util.stream.Stream;

/**
 * Represents the sort() function that sorts array elements.
 * Returns a new sorted array, or throws an error if input is not an array.
 */
public class Sort implements Expression {
  static {
    BuiltinRegistry.register("sort", 0);
  }

  @Override
  public Stream<JqValue> evaluate(JqValue input) {
    if (!input.isArray()) {
      throw new RuntimeException("Cannot sort non-array type: " + input.type());
    }

    return Stream.of(input.sort());
  }
}