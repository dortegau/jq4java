package com.dortegau.jq4java.ast;

import com.dortegau.jq4java.json.JqValue;
import java.util.stream.Stream;

/** Implements jq's @sh formatter. */
public class ShellFormat implements Expression {
  @Override
  public Stream<JqValue> evaluate(JqValue input) {
    String formatted = FormatUtils.formatShell(input);
    return Stream.of(JqValue.fromString(formatted));
  }
}
