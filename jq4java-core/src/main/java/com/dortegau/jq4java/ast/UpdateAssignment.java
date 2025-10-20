package com.dortegau.jq4java.ast;

import com.dortegau.jq4java.json.JqValue;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents update-assignment expressions like ".foo += 1".
 */
public class UpdateAssignment implements Expression {
  private final UpdatableExpression target;
  private final String operator;
  private final Expression valueExpression;

  /**
   * Creates a new update-assignment expression.
   *
   * @param target the expression that selects the value to update
   * @param operator the update operator token (e.g. "+=")
   * @param valueExpression the expression providing the right-hand side value
   */
  public UpdateAssignment(UpdatableExpression target, String operator, Expression valueExpression) {
    this.target = target;
    this.operator = operator;
    this.valueExpression = valueExpression;
  }

  @Override
  public Stream<JqValue> evaluate(JqValue input) {
    List<JqValue> values = valueExpression.evaluate(input).collect(Collectors.toList());
    if (values.isEmpty()) {
      throw new RuntimeException("Update expression produced no results");
    }
    if (values.size() > 1) {
      throw new RuntimeException("Update expression produced multiple results");
    }

    JqValue rightValue = values.get(0);
    JqValue updated = target.update(input, current -> applyOperator(current, rightValue));
    return Stream.of(updated);
  }

  private JqValue applyOperator(JqValue left, JqValue right) {
    switch (operator) {
      case "+=":
        return left.add(right);
      case "-=":
        return left.subtract(right);
      case "*=":
        return left.multiply(right);
      case "/=":
        return left.divide(right);
      case "%=":
        return left.modulo(right);
      case "//=":
        return left.isTruthy() ? left : right;
      default:
        throw new RuntimeException("Unsupported update operator: " + operator);
    }
  }
}
