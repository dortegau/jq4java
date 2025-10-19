package com.dortegau.jq4java.ast;

import com.dortegau.jq4java.json.JqValue;
import java.util.stream.Stream;

/** Implements jq's @tsv formatter. */
public class TsvFormat implements Expression {
  @Override
  public Stream<JqValue> evaluate(JqValue input) {
    String formatted = FormatUtils.formatTsvRow(input);
    return Stream.of(JqValue.fromString(formatted));
  }
}
