package com.dortegau.jq4java.ast;

import com.dortegau.jq4java.json.JqValue;
import java.util.stream.Stream;

/**
 * Represents the select() function that filters values based on a condition.
 * Values are passed through if the condition evaluates to truthy, otherwise filtered out.
 */
public class Select implements Expression {
  private final Expression condition;

  static {
    BuiltinRegistry.register("select", 1);
  }

  /**
   * Creates a new Select expression with the given condition.
   *
   * @param condition the condition expression to evaluate
   */
  public Select(Expression condition) {
    this.condition = condition;
  }

  @Override
  public Stream<JqValue> evaluate(JqValue input) {
    return condition.evaluate(input)
        .flatMap(conditionResult -> {
          if (conditionResult.isTruthy()) {
            return Stream.of(input);
          } else {
            return Stream.empty();
          }
        });
  }
}