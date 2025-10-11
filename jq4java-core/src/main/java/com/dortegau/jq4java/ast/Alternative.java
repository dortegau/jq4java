package com.dortegau.jq4java.ast;

import com.dortegau.jq4java.json.JqValue;
import java.util.stream.Stream;

/**
 * Alternative operator (//): returns right if left is null or false.
 */
public class Alternative implements Expression {
  private final Expression left;
  private final Expression right;

  public Alternative(Expression left, Expression right) {
    this.left = left;
    this.right = right;
  }

  @Override
  public Stream<JqValue> evaluate(JqValue input) {
    Stream<JqValue> leftResult = left.evaluate(input);
    JqValue leftValue = leftResult.findFirst().orElse(JqValue.nullValue());
    
    if (isNullOrFalse(leftValue)) {
      return right.evaluate(input);
    }
    return Stream.of(leftValue);
  }

  private boolean isNullOrFalse(JqValue value) {
    String str = value.toString();
    return str.equals("null") || str.equals("false");
  }
}
