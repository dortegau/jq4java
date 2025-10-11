package com.dortegau.jq4java.ast;

import com.dortegau.jq4java.json.JqValue;
import java.util.stream.Stream;

/**
 * Array iteration expression.
 */
public class ArrayIteration implements Expression {
  private final Expression base;

  public ArrayIteration(Expression base) {
    this.base = base;
  }

  @Override
  public Stream<JqValue> evaluate(JqValue input) {
    return base.evaluate(input).flatMap(JqValue::stream);
  }
}