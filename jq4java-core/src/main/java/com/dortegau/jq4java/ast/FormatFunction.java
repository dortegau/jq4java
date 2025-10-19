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
      case "base64":
        return new Base64Encode().evaluate(input);
      case "base64d":
        return new Base64Decode().evaluate(input);
      case "uri":
        return new UriEncode().evaluate(input);
      case "urid":
        return new UriDecode().evaluate(input);
      default:
        throw new RuntimeException("Unknown format: @" + formatName);
    }
  }
}
