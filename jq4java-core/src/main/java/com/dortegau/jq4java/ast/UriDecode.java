package com.dortegau.jq4java.ast;

import com.dortegau.jq4java.json.JqValue;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

/**
 * Implementation of the @uri URI decode function.
 * Decodes URI-encoded strings using UTF-8 encoding.
 */
public class UriDecode implements Expression {

  @Override
  public Stream<JqValue> evaluate(JqValue input) {
    if (!input.isString()) {
      throw new RuntimeException(
          "Cannot uri decode " + input.typeName() + " (" + input + ")");
    }

    try {
      String decoded = URLDecoder.decode(input.asString(), StandardCharsets.UTF_8.name());
      return Stream.of(JqValue.fromString(decoded));
    } catch (IllegalArgumentException e) {
      throw new RuntimeException("Invalid percent-encoded string: " + input, e);
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException("Failed to uri decode string", e);
    }
  }
}
