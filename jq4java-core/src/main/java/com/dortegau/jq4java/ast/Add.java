package com.dortegau.jq4java.ast;

import com.dortegau.jq4java.json.JqValue;
import java.util.stream.Stream;

public class Add implements Expression {
  static {
    BuiltinRegistry.register("add", 0);
  }

  @Override
  public Stream<JqValue> evaluate(JqValue input) {
    if (input.isNull()) {
      throw new RuntimeException("Cannot iterate over null (null)");
    }

    if (!input.isArray()) {
      throw new RuntimeException("Cannot iterate over " + input.type() + " (" + input + ")");
    }

    return Stream.of(input.add());
  }
}