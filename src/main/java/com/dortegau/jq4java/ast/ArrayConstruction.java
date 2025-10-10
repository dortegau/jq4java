package com.dortegau.jq4java.ast;

import com.dortegau.jq4java.json.JqValue;
import java.util.List;
import java.util.stream.Stream;

/**
 * Array construction expression.
 */
public class ArrayConstruction implements Expression {
  private final List<Expression> elements;

  public ArrayConstruction(List<Expression> elements) {
    this.elements = elements;
  }

  @Override
  public Stream<JqValue> evaluate(JqValue input) {
    List<JqValue> values = elements.stream()
        .flatMap(expr -> expr.evaluate(input))
        .collect(java.util.stream.Collectors.toList());
    return Stream.of(JqValue.array(values));
  }
}