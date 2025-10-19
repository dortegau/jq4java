package com.dortegau.jq4java.ast;

import com.dortegau.jq4java.json.JqValue;
import java.util.stream.Stream;

public class FormatFunction implements Expression {
  private final String formatName;

  public FormatFunction(String formatName) {
    this.formatName = formatName;
  }

  @Override
  public Stream<JqValue> evaluate(JqValue input) {
    switch (formatName) {
      case "uri":
        return new UriEncode().evaluate(input);
      case "urid":
        return new UriDecode().evaluate(input);
      default:
        throw new RuntimeException("Unknown format: @" + formatName);
    }
  }
}
