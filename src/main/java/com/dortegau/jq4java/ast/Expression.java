package com.dortegau.jq4java.ast;

import com.dortegau.jq4java.json.JqValue;
import java.util.stream.Stream;

/**
 * Base interface for all jq expressions.
 */
public interface Expression {
  /**
   * Evaluates this expression against the given input.
   *
   * @param input the input value
   * @return a stream of result values
   */
  Stream<JqValue> evaluate(JqValue input);
}
