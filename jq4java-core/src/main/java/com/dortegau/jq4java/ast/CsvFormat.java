package com.dortegau.jq4java.ast;

import com.dortegau.jq4java.json.JqValue;
import java.util.stream.Stream;

/** Implements jq's @csv formatter. */
public class CsvFormat implements Expression {
  @Override
  public Stream<JqValue> evaluate(JqValue input) {
    String formatted = FormatUtils.formatCsvRow(input);
    return Stream.of(JqValue.fromString(formatted));
  }
}
