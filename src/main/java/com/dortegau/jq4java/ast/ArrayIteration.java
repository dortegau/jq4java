package com.dortegau.jq4java.ast;

import com.dortegau.jq4java.json.JqValue;
import java.util.stream.Stream;

/**
 * Array iteration expression.
 */
public class ArrayIteration implements Expression {
  @Override
  public Stream<JqValue> evaluate(JqValue input) {
    return input.stream();
  }
}