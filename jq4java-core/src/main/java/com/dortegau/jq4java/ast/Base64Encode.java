package com.dortegau.jq4java.ast;

import com.dortegau.jq4java.json.JqValue;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Implementation of the @base64 base64 encode function.
 * Encodes strings or byte arrays to Base64 format.
 */
public class Base64Encode implements Expression {

  @Override
  public Stream<JqValue> evaluate(JqValue input) {
    if (input.isString()) {
      return Stream.of(encodeBytes(input.asString().getBytes(StandardCharsets.UTF_8)));
    }

    if (input.isArray()) {
      List<JqValue> elements = input.stream().collect(Collectors.toList());
      byte[] bytes = new byte[elements.size()];
      for (int i = 0; i < elements.size(); i++) {
        JqValue element = elements.get(i);
        if (!element.isNumber()) {
          throw new RuntimeException(
              "Cannot base64 encode array containing non-number at index " + i + ": " + element);
        }
        double numericValue = element.asNumber();
        if (numericValue < 0 || numericValue > 255 || numericValue != Math.floor(numericValue)) {
          throw new RuntimeException(
              "Cannot base64 encode array containing non-byte value at index "
                  + i
                  + ": "
                  + element);
        }
        bytes[i] = (byte) ((int) numericValue);
      }
      return Stream.of(encodeBytes(bytes));
    }

    throw new RuntimeException(
        "Cannot base64 encode " + input.typeName() + " (" + input + ")");
  }

  private JqValue encodeBytes(byte[] bytes) {
    String encoded = Base64.getEncoder().encodeToString(bytes);
    return JqValue.fromString(encoded);
  }
}
