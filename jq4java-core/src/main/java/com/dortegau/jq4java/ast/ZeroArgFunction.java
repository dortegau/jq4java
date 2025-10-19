package com.dortegau.jq4java.ast;

import com.dortegau.jq4java.json.JqValue;
import java.util.stream.Stream;

/**
 * Generic implementation for zero-argument builtin functions.
 * Dispatches function calls to the appropriate specific implementations.
 */
public class ZeroArgFunction implements Expression {
  private final String functionName;

  /**
   * Creates a zero-argument function with the specified name.
   *
   * @param functionName the name of the function to call
   */
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
      case "length":
        return new Length().evaluate(input);
      case "keys":
        return new Keys().evaluate(input);
      case "type":
        return new Type().evaluate(input);
      case "builtins":
        return new Builtins().evaluate(input);
      case "to_entries":
        return new ToEntries().evaluate(input);
      case "from_entries":
        return new FromEntries().evaluate(input);
      case "range":
        // Range without arguments should throw an error
        throw new RuntimeException("range/0 is not defined");
      default:
        throw new RuntimeException("Unknown zero-argument function: " + functionName);
    }
  }
}
