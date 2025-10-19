package com.dortegau.jq4java;

import com.dortegau.jq4java.ast.Expression;
import com.dortegau.jq4java.json.JqValue;
import com.dortegau.jq4java.json.OrgJsonValue;
import com.dortegau.jq4java.parser.JqParser;

/**
 * Main entry point for jq4java library.
 */
public class Jq {
  /**
   * Executes a jq expression on JSON input.
   *
   * @param program the jq expression to execute
   * @param input the JSON input string
   * @return the result as a JSON string
   */
  public static String execute(String program, String input) {
    Expression expr = compile(program);
    return execute(expr, input);
  }

  /**
   * Compiles a jq expression for reuse across multiple executions.
   *
   * @param program the jq expression to compile
   * @return the compiled expression AST
   */
  public static Expression compile(String program) {
    return JqParser.parse(program);
  }

  /**
   * Executes a precompiled jq expression on JSON input.
   *
   * @param expression the precompiled jq expression
   * @param input the JSON input string
   * @return the result as a JSON string
   */
  public static String execute(Expression expression, String input) {
    JqValue inputValue = OrgJsonValue.parse(input);
    return execute(expression, inputValue);
  }

  /**
   * Executes a precompiled jq expression on JSON input value.
   *
   * @param expression the precompiled jq expression
   * @param inputValue the JSON input value
   * @return the result as a JSON string
   */
  public static String execute(Expression expression, JqValue inputValue) {
    StringBuilder sb = new StringBuilder();
    expression.evaluate(inputValue).forEach(value -> {
      if (sb.length() > 0) {
        sb.append('\n');
      }
      sb.append(value.toJson());
    });
    return sb.toString();
  }
}
