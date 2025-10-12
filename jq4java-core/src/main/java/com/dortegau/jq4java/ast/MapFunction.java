package com.dortegau.jq4java.ast;

import com.dortegau.jq4java.json.JqValue;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MapFunction implements Expression {
  static {
    BuiltinRegistry.register("map", 1);
  }

  private final Expression expr;

  public MapFunction(Expression expr) {
    this.expr = expr;
  }

  @Override
  public Stream<JqValue> evaluate(JqValue input) {
    if (!input.isArray()) {
      throw new RuntimeException("Cannot iterate over " + input.type().toString().replace("\"", "") + " (" + input + ")");
    }

    List<JqValue> results = input.stream()
        .flatMap(item -> expr.evaluate(item))
        .collect(Collectors.toList());

    return Stream.of(JqValue.array(results));
  }
}
