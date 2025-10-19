package com.dortegau.jq4java.ast;

import com.dortegau.jq4java.json.JqValue;
import java.util.stream.Stream;

/** Implements jq's @json formatter. */
public class JsonFormat implements Expression {
  @Override
  public Stream<JqValue> evaluate(JqValue input) {
    return Stream.of(JqValue.fromString(FormatUtils.toJson(input)));
  }
}
