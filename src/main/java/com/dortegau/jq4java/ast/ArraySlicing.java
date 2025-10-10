package com.dortegau.jq4java.ast;

import com.dortegau.jq4java.json.JqValue;
import java.util.stream.Stream;

/**
 * Array slicing expression.
 */
public class ArraySlicing implements Expression {
  private final Integer start;
  private final Integer end;
  private final Expression base;

  /**
   * Creates an array slicing expression.
   *
   * @param start start index (null for beginning)
   * @param end end index (null for end)
   * @param base base expression
   */
  public ArraySlicing(Integer start, Integer end, Expression base) {
    this.start = start;
    this.end = end;
    this.base = base;
  }

  @Override
  public Stream<JqValue> evaluate(JqValue input) {
    return base.evaluate(input).map(value -> value.slice(start, end));
  }
}