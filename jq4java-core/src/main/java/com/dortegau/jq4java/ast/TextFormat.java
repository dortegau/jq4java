package com.dortegau.jq4java.ast;

import com.dortegau.jq4java.json.JqValue;
import java.util.stream.Stream;

/** Implements jq's @text formatter. */
public class TextFormat implements Expression {
  @Override
  public Stream<JqValue> evaluate(JqValue input) {
    String text = FormatUtils.toText(input);
    return Stream.of(JqValue.fromString(text));
  }
}
