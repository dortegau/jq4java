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
    Expression expr = JqParser.parse(program);
    JqValue inputValue = OrgJsonValue.parse(input);
    StringBuilder sb = new StringBuilder();
    expr.evaluate(inputValue).forEach(value -> {
      if (sb.length() > 0) {
        sb.append('\n');
      }
      sb.append(value.toJson());
    });
    return sb.toString();
  }
}
