package com.dortegau.jq4java.ast;

import com.dortegau.jq4java.json.JqValue;
import java.util.stream.Stream;

/**
 * Identity expression that returns the input unchanged.
 */
public class Identity implements Expression {
  @Override
  public Stream<JqValue> evaluate(JqValue input) {
    return Stream.of(input);
  }
}
