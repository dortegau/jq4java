package com.dortegau.jq4java.ast;

import com.dortegau.jq4java.json.JqValue;
import java.util.stream.Stream;

/**
 * Implementation of the utf8bytelength function.
 * Returns the number of bytes needed to represent a string in UTF-8.
 */
public class Utf8ByteLength implements Expression {
  static {
    BuiltinRegistry.register("utf8bytelength", 0);
  }

  @Override
  public Stream<JqValue> evaluate(JqValue input) {
    return Stream.of(input.utf8ByteLength());
  }
}
