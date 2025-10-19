package com.dortegau.jq4java.ast;

import com.dortegau.jq4java.json.JqValue;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WithEntries implements Expression {
  static {
    BuiltinRegistry.register("with_entries", 1);
  }

  private final Expression expr;

  public WithEntries(Expression expr) {
    this.expr = expr;
  }

  @Override
  public Stream<JqValue> evaluate(JqValue input) {
    ToEntries toEntries = new ToEntries();
    JqValue entries =
        toEntries
            .evaluate(input)
            .findFirst()
            .orElseThrow(
                () -> new RuntimeException("with_entries could not convert input to entries"));

    if (!entries.isArray()) {
      throw new RuntimeException("with_entries expects to_entries to produce an array");
    }

    List<JqValue> mappedEntries =
        entries.stream()
            .flatMap(entry -> expr.evaluate(entry))
            .collect(Collectors.toList());

    JqValue mappedArray = JqValue.array(mappedEntries);

    FromEntries fromEntries = new FromEntries();
    return fromEntries.evaluate(mappedArray);
  }
}
