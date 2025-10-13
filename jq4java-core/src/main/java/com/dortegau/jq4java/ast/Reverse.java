package com.dortegau.jq4java.ast;

import com.dortegau.jq4java.json.JqValue;
import java.util.stream.Stream;

public class Reverse implements Expression {
  static {
    BuiltinRegistry.register("reverse", 0);
  }

  @Override
  public Stream<JqValue> evaluate(JqValue input) {
    if (!input.isArray()) {
      throw new RuntimeException("Cannot reverse non-array type: " + input.type());
    }

    return Stream.of(input.reverse());
  }
}