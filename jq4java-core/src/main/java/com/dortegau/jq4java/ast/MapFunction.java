package com.dortegau.jq4java.ast;

import com.dortegau.jq4java.json.JqValue;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Implementation of the map function.
 * Applies an expression to each element of an array and returns the results as a new array.
 */
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
      String typeName = input.type().toString().replace("\"", "");
      throw new RuntimeException("Cannot iterate over " + typeName + " (" + input + ")");
    }

    List<JqValue> results = input.stream()
        .flatMap(item -> expr.evaluate(item))
        .collect(Collectors.toList());

    return Stream.of(JqValue.array(results));
  }
}
