package com.dortegau.jq4java.ast;

import com.dortegau.jq4java.json.JqValue;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Represents comparison operations between two expressions.
 * Supports ==, !=, <, <=, >, >= operators.
 */
public class Comparison implements Expression {
  private final Expression left;
  private final String operator;
  private final Expression right;

  /**
   * Creates a new Comparison expression.
   *
   * @param left the left-hand side expression
   * @param operator the comparison operator
   * @param right the right-hand side expression
   */
  public Comparison(Expression left, String operator, Expression right) {
    this.left = left;
    this.operator = operator;
    this.right = right;
  }

  @Override
  public Stream<JqValue> evaluate(JqValue input) {
    List<JqValue> results = new ArrayList<>();
    left.evaluate(input).forEach(leftValue -> {
      right.evaluate(input).forEach(rightValue -> {
        results.add(JqValue.fromBoolean(compare(leftValue, rightValue)));
      });
    });
    return results.stream();
  }

  private boolean compare(JqValue left, JqValue right) {
    switch (operator) {
      case "==":
        return left.equals(right);
      case "!=":
        return !left.equals(right);
      case "<":
        return left.compareTo(right) < 0;
      case "<=":
        return left.compareTo(right) <= 0;
      case ">":
        return left.compareTo(right) > 0;
      case ">=":
        return left.compareTo(right) >= 0;
      default:
        throw new RuntimeException("Unknown operator: " + operator);
    }
  }
}
