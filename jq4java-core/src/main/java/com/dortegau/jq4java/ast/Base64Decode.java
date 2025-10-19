package com.dortegau.jq4java.ast;

import com.dortegau.jq4java.json.JqValue;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.stream.Stream;

public class Base64Decode implements Expression {
  static {
    BuiltinRegistry.register("base64d", 0);
  }

  @Override
  public Stream<JqValue> evaluate(JqValue input) {
    if (!input.isString()) {
      throw new RuntimeException(
          "Cannot base64 decode " + input.typeName() + " (" + input + ")");
    }

    try {
      byte[] decoded = Base64.getDecoder().decode(input.asString());
      String result = new String(decoded, StandardCharsets.UTF_8);
      return Stream.of(JqValue.fromString(result));
    } catch (IllegalArgumentException e) {
      throw new RuntimeException("Invalid base64 string: " + input, e);
    }
  }
}
