package com.dortegau.jq4java.ast;

import com.dortegau.jq4java.json.JqValue;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WithEntries implements Expression {
  static {
    BuiltinRegistry.register("with_entries", 1);
  }

  private final Expression pipeline;

  // Singleton instances for stateless transformers
  private static final ToEntries TO_ENTRIES = new ToEntries();
  private static final FromEntries FROM_ENTRIES = new FromEntries();

  public WithEntries(Expression expr) {
    // Compose the pipeline: to_entries | map(expr) | from_entries
    this.pipeline = new PipeExpression(
        TO_ENTRIES,
        new MapFunction(expr),
        FROM_ENTRIES
    );
  }

  @Override
  public Stream<JqValue> evaluate(JqValue input) {
    return pipeline.evaluate(input);
  }
}
