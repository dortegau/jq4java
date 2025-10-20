package com.dortegau.jq4java.ast;

import com.dortegau.jq4java.json.JqValue;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Implementation of the map_values function.
 * Applies an expression to each value in an array or object, returning the first result.
 */
public class MapValuesFunction implements Expression {
  static {
    BuiltinRegistry.register("map_values", 1);
  }

  private final Expression expr;

  public MapValuesFunction(Expression expr) {
    this.expr = expr;
  }

  @Override
  public Stream<JqValue> evaluate(JqValue input) {
    if (input.isArray()) {
      List<JqValue> results = new ArrayList<>();
      input.stream()
          .forEach(
              item -> expr.evaluate(item).findFirst().ifPresent(results::add));
      return Stream.of(JqValue.array(results));
    }

    if ("object".equals(input.typeName())) {
      Map<String, JqValue> mapped = new LinkedHashMap<>();
      JqValue keys = input.keys();
      keys.stream()
          .forEach(
              keyValue -> {
                String key = keyValue.isString() ? keyValue.asString() : keyValue.toString();
                Optional<JqValue> maybeValue = expr.evaluate(input.get(key)).findFirst();
                maybeValue.ifPresent(value -> mapped.put(key, value));
              });
      return Stream.of(JqValue.object(mapped));
    }

    String typeName = input.type().toString().replace("\"", "");
    throw new RuntimeException("Cannot iterate over " + typeName + " (" + input + ")");
  }
}
