package com.dortegau.jq4java.ast;

import com.dortegau.jq4java.json.JqValue;
import java.util.stream.Stream;

/** Implements jq's @html formatter. */
public class HtmlFormat implements Expression {
  @Override
  public Stream<JqValue> evaluate(JqValue input) {
    String escaped = FormatUtils.escapeHtml(FormatUtils.toText(input));
    return Stream.of(JqValue.fromString(escaped));
  }
}
