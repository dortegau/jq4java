package com.dortegau.jq4java.ast;

import com.dortegau.jq4java.json.JqValue;
import java.util.stream.Stream;

/** Builtin that converts the current input to a JSON string. */
public class ToJson implements Expression {
  static {
    BuiltinRegistry.register("tojson", 0);
  }

  @Override
  public Stream<JqValue> evaluate(JqValue input) {
    return Stream.of(JqValue.fromString(input.toJson()));
  }
}
