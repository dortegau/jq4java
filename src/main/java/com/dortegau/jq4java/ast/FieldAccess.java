package com.dortegau.jq4java.ast;

import com.dortegau.jq4java.json.JqValue;
import java.util.stream.Stream;

/**
 * Field access expression.
 */
public class FieldAccess implements Expression {
  private final String fieldName;
  private final Expression base;

  public FieldAccess(String fieldName) {
    this.fieldName = fieldName;
    this.base = new Identity();
  }

  public FieldAccess(String fieldName, Expression base) {
    this.fieldName = fieldName;
    this.base = base;
  }

  @Override
  public Stream<JqValue> evaluate(JqValue input) {
    return base.evaluate(input).map(value -> value.get(fieldName));
  }
}