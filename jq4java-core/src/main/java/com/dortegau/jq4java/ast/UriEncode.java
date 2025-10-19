package com.dortegau.jq4java.ast;

import com.dortegau.jq4java.json.JqValue;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

/**
 * Implementation of the @uri URI encode function.
 * Encodes strings for use in URIs using UTF-8 encoding.
 */
public class UriEncode implements Expression {

  @Override
  public Stream<JqValue> evaluate(JqValue input) {
    if (!input.isString()) {
      throw new RuntimeException(
          "Cannot uri encode " + input.typeName() + " (" + input + ")");
    }

    try {
      String encoded = URLEncoder.encode(input.asString(), StandardCharsets.UTF_8.name());
      encoded = encoded.replace("+", "%20");
      return Stream.of(JqValue.fromString(encoded));
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException("Failed to uri encode string", e);
    }
  }
}
