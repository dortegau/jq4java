package com.dortegau.jq4java.ast;

import com.dortegau.jq4java.json.JqValue;
import java.util.stream.Stream;

public class Keys implements Expression {
  static {
    BuiltinRegistry.register("keys", 0);
  }

  @Override
  public Stream<JqValue> evaluate(JqValue input) {
    return Stream.of(input.keys());
  }
}
