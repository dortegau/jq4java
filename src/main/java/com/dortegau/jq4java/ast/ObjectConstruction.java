package com.dortegau.jq4java.ast;

import com.dortegau.jq4java.json.JqValue;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Object construction expression.
 */
public class ObjectConstruction implements Expression {
  private final Map<String, Expression> fields;

  public ObjectConstruction(Map<String, Expression> fields) {
    this.fields = fields;
  }

  @Override
  public Stream<JqValue> evaluate(JqValue input) {
    Map<String, JqValue> result = new LinkedHashMap<>();
    for (Map.Entry<String, Expression> entry : fields.entrySet()) {
      String key = entry.getKey();
      Expression expr = entry.getValue();
      JqValue value = expr.evaluate(input).findFirst().orElse(JqValue.nullValue());
      result.put(key, value);
    }
    return Stream.of(JqValue.object(result));
  }
}