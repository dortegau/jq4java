package com.dortegau.jq4java.ast;

import com.dortegau.jq4java.json.JqValue;
import java.util.stream.Stream;

/**
 * Implements the jq recursive descent operator (`..`) and the zero-argument recurse builtin.
 */
public class RecursiveDescent implements Expression {
  static {
    BuiltinRegistry.register("recurse", 0);
  }

  @Override
  public Stream<JqValue> evaluate(JqValue input) {
    return descend(input);
  }

  private Stream<JqValue> descend(JqValue value) {
    Stream<JqValue> children;
    String type = value.typeName();
    if ("array".equals(type)) {
      children = value.stream().flatMap(this::descend);
    } else if ("object".equals(type)) {
      children = value.keys().stream()
          .filter(JqValue::isString)
          .map(keyValue -> value.get(keyValue.asString()))
          .flatMap(this::descend);
    } else {
      children = Stream.empty();
    }
    return Stream.concat(Stream.of(value), children);
  }
}
