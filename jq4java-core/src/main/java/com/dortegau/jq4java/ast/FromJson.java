package com.dortegau.jq4java.ast;

import com.dortegau.jq4java.json.JqValue;
import com.dortegau.jq4java.json.OrgJsonValue;
import java.util.stream.Stream;
import org.json.JSONException;

/** Builtin that parses the current string input as JSON. */
public class FromJson implements Expression {
  static {
    BuiltinRegistry.register("fromjson", 0);
  }

  @Override
  public Stream<JqValue> evaluate(JqValue input) {
    if (!input.isString()) {
      throw new RuntimeException("fromjson requires string input");
    }

    String jsonText = input.asString();
    try {
      return Stream.of(OrgJsonValue.parse(jsonText));
    } catch (JSONException e) {
      throw new RuntimeException("Invalid JSON text for fromjson: " + jsonText, e);
    }
  }
}
