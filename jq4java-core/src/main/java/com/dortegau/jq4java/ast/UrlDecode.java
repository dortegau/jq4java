package com.dortegau.jq4java.ast;

import com.dortegau.jq4java.json.JqValue;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

public class UrlDecode implements Expression {
  static {
    BuiltinRegistry.register("urldecode", 0);
  }

  @Override
  public Stream<JqValue> evaluate(JqValue input) {
    if (!input.isString()) {
      throw new RuntimeException(
          "Cannot url decode " + input.typeName() + " (" + input + ")");
    }

    try {
      String decoded = URLDecoder.decode(input.asString(), StandardCharsets.UTF_8.toString());
      return Stream.of(JqValue.fromString(decoded));
    } catch (IllegalArgumentException e) {
      throw new RuntimeException("Invalid percent-encoded string: " + input, e);
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException("Failed to url decode string", e);
    }
  }
}
