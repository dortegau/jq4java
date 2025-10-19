package com.dortegau.jq4java.ast;

import com.dortegau.jq4java.json.JqValue;
import java.util.stream.Stream;

/**
 * Implementation of the with_entries function.
 * Converts an object to key-value pairs, applies a transformation, then converts back to an object.
 */
public class WithEntries implements Expression {
  static {
    BuiltinRegistry.register("with_entries", 1);
  }

  private final Expression pipeline;

  public WithEntries(Expression mapper) {
    this.pipeline =
        new Pipe(new ToEntries(), new Pipe(new MapFunction(mapper), new FromEntries()));
  }

  @Override
  public Stream<JqValue> evaluate(JqValue input) {
    return pipeline.evaluate(input);
  }
}
