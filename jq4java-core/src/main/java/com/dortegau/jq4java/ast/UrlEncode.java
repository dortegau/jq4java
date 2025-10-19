package com.dortegau.jq4java.ast;

import com.dortegau.jq4java.json.JqValue;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

public class UrlEncode implements Expression {
  static {
    BuiltinRegistry.register("urlencode", 0);
  }

  @Override
  public Stream<JqValue> evaluate(JqValue input) {
    if (!input.isString()) {
      throw new RuntimeException(
          "Cannot url encode " + input.typeName() + " (" + input + ")");
    }

    try {
      String encoded = URLEncoder.encode(input.asString(), StandardCharsets.UTF_8.toString());
      encoded = encoded.replace("+", "%20");
      return Stream.of(JqValue.fromString(encoded));
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException("Failed to url encode string", e);
    }
  }
}
