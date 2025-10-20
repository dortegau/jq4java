package com.dortegau.jq4java.ast;

import com.dortegau.jq4java.json.JqValue;
import java.util.function.Function;

/**
 * Represents an expression that can update its target within an input value.
 */
public interface UpdatableExpression extends Expression {
  /**
   * Applies the provided updater to the value targeted by this expression.
   *
   * @param input the original input value
   * @param updater a function that receives the current targeted value
   *                and returns the updated value
   * @return a new {@link JqValue} reflecting the applied update
   */
  JqValue update(JqValue input, Function<JqValue, JqValue> updater);
}
