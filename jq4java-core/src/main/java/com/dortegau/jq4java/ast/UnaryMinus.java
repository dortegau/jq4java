package com.dortegau.jq4java.ast;

import com.dortegau.jq4java.json.JqValue;
import java.util.stream.Stream;

/**
 * Implementation of the unary minus operator.
 * Negates numeric values by subtracting them from zero.
 */
public class UnaryMinus implements Expression {
  private final Expression operand;

  public UnaryMinus(Expression operand) {
    this.operand = operand;
  }

  @Override
  public Stream<JqValue> evaluate(JqValue input) {
    return operand.evaluate(input).map(value -> {
      if (!value.isNumber()) {
        String valueType = value.isNull() ? "null" :
                          value.type().toString().replace("\"", "");
        throw new RuntimeException(valueType + " cannot be negated");
      }
      // Subtract the value from zero to get the negation
      return JqValue.fromDouble(0).subtract(value);
    });
  }
}