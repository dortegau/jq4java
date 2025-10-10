package com.dortegau.jq4java.ast;

import com.dortegau.jq4java.json.JqValue;
import java.util.stream.Stream;

/**
 * Array indexing expression.
 */
public class ArrayIndexing implements Expression {
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
}