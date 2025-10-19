package com.dortegau.jq4java.ast;

import com.dortegau.jq4java.json.JqValue;
import com.dortegau.jq4java.json.OrgJsonValue;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Implementation of the builtins function that returns an array
 * of all available builtin function names.
 */
public class Builtins implements Expression {
  static {
    BuiltinRegistry.register("builtins", 0);
  }

  @Override
  public Stream<JqValue> evaluate(JqValue input) {
    List<JqValue> functions = BuiltinRegistry.list().stream()
        .map(name -> OrgJsonValue.literal("\"" + name + "\""))
        .collect(Collectors.toList());
    return Stream.of(OrgJsonValue.array(functions));
  }
}
