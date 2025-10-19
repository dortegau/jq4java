package com.dortegau.jq4java.ast;

import com.dortegau.jq4java.json.JqValue;
import java.util.stream.Stream;

public class ZeroArgFunction implements Expression {
  private final String functionName;

  public ZeroArgFunction(String functionName) {
    this.functionName = functionName;
    if (!BuiltinRegistry.list().contains(functionName + "/0")) {
      throw new RuntimeException(functionName + "/0 is not defined");
    }
  }

  @Override
  public Stream<JqValue> evaluate(JqValue input) {
    switch (functionName) {
      case "flatten":
        return new Flatten().evaluate(input);
      case "add":
        return new Add().evaluate(input);
      case "abs":
        return new Abs().evaluate(input);
      case "sort":
        return new Sort().evaluate(input);
      case "reverse":
        return new Reverse().evaluate(input);
      case "unique":
        return new Unique().evaluate(input);
      case "transpose":
        return new Transpose().evaluate(input);
      default:
        throw new RuntimeException("Unknown zero-argument function: " + functionName);
    }
  }
}
