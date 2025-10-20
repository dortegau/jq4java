package com.dortegau.jq4java.ast;

import com.dortegau.jq4java.json.JqValue;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Field access expression.
 */
public class FieldAccess implements UpdatableExpression {
  private final String fieldName;
  private final Expression base;

  public FieldAccess(String fieldName, Expression base) {
    this.fieldName = fieldName;
    this.base = base;
  }

  @Override
  public Stream<JqValue> evaluate(JqValue input) {
    return base.evaluate(input).map(value -> value.get(fieldName));
  }

  @Override
  public JqValue update(JqValue input, Function<JqValue, JqValue> updater) {
    if (!(base instanceof UpdatableExpression)) {
      throw new RuntimeException("Base expression is not updatable");
    }

    UpdatableExpression updatableBase = (UpdatableExpression) base;
    return updatableBase.update(input, currentBase -> {
      JqValue currentValue = currentBase.get(fieldName);
      JqValue updatedValue = updater.apply(currentValue);
      return currentBase.set(fieldName, updatedValue);
    });
  }
}