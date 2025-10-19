package com.dortegau.jq4java.ast;

import com.dortegau.jq4java.json.JqValue;
import java.util.stream.Stream;

/**
 * Represents arithmetic operations between two expressions.
 * Supports +, -, *, /, % operators.
 */
public class Arithmetic implements Expression {
  private final Expression left;
  private final String operator;
  private final Expression right;

  /**
   * Creates a new Arithmetic expression.
   *
   * @param left the left-hand side expression
   * @param operator the arithmetic operator (+, -, *, /, %)
   * @param right the right-hand side expression
   */
  public Arithmetic(Expression left, String operator, Expression right) {
    this.left = left;
    this.operator = operator;
    this.right = right;
  }

  @Override
  public Stream<JqValue> evaluate(JqValue input) {
    return left.evaluate(input).flatMap(leftValue ->
        right.evaluate(input).map(rightValue -> {
          switch (operator) {
            case "+":
              return leftValue.add(rightValue);
            case "-":
              return leftValue.subtract(rightValue);
            case "*":
              return leftValue.multiply(rightValue);
            case "/":
              return leftValue.divide(rightValue);
            case "%":
              return leftValue.modulo(rightValue);
            default:
              throw new RuntimeException("Unknown arithmetic operator: " + operator);
          }
        })
    );
  }
}
