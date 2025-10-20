package com.dortegau.jq4java.ast;

import com.dortegau.jq4java.json.JqValue;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Array indexing expression.
 */
public class ArrayIndexing implements UpdatableExpression {
  private final int index;
  private final Expression base;

  public ArrayIndexing(int index, Expression base) {
    this.index = index;
    this.base = base;
  }

  @Override
  public Stream<JqValue> evaluate(JqValue input) {
    return base.evaluate(input).map(value -> value.get(index));
  }

  @Override
  public JqValue update(JqValue input, Function<JqValue, JqValue> updater) {
    if (!(base instanceof UpdatableExpression)) {
      throw new RuntimeException("Base expression is not updatable");
    }

    UpdatableExpression updatableBase = (UpdatableExpression) base;
    return updatableBase.update(input, currentBase -> {
      JqValue currentValue = currentBase.get(index);
      JqValue updatedValue = updater.apply(currentValue);
      return currentBase.set(index, updatedValue);
    });
  }
}
