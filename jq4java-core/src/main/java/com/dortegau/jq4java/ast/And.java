package com.dortegau.jq4java.ast;

import com.dortegau.jq4java.json.JqValue;
import java.util.stream.Stream;

public class And implements Expression {
  private final Expression left;
  private final Expression right;

  public And(Expression left, Expression right) {
    this.left = left;
    this.right = right;
  }

  @Override
  public Stream<JqValue> evaluate(JqValue input) {
    return left.evaluate(input).flatMap(leftValue -> {
      if (!leftValue.isTruthy()) {
        return Stream.of(JqValue.fromBoolean(false));
      }
      return right.evaluate(input).map(rightValue ->
          JqValue.fromBoolean(rightValue.isTruthy())
      );
    });
  }
}
