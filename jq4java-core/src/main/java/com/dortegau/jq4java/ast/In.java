package com.dortegau.jq4java.ast;

import com.dortegau.jq4java.json.JqValue;
import java.util.stream.Stream;

/**
 * Implementation of the in function.
 * Checks whether the input key or index exists in the provided object or array.
 */
public class In implements Expression {
  static {
    BuiltinRegistry.register("in", 1);
  }

  private final Expression containerExpression;

  public In(Expression containerExpression) {
    this.containerExpression = containerExpression;
  }

  @Override
  public Stream<JqValue> evaluate(JqValue input) {
    return containerExpression.evaluate(input)
        .map(container -> evaluateContainer(input, container));
  }

  private JqValue evaluateContainer(JqValue key, JqValue container) {
    if (container.isNull()) {
      return JqValue.fromBoolean(false);
    }

    String containerType = container.typeName();
    String keyType = key.typeName();

    if ("object".equals(containerType)) {
      if (!key.isString()) {
        throw new RuntimeException("Cannot check whether object has a " + keyType + " key");
      }
      JqValue keys = container.keys();
      boolean exists = keys.stream().anyMatch(candidate -> candidate.equals(key));
      return JqValue.fromBoolean(exists);
    }

    if (container.isArray()) {
      if (!key.isNumber()) {
        throw new RuntimeException("Cannot check whether array has a " + keyType + " key");
      }
      double numericKey = key.asNumber();
      if (Double.isNaN(numericKey) || Double.isInfinite(numericKey)) {
        return JqValue.fromBoolean(false);
      }
      if (numericKey > Integer.MAX_VALUE || numericKey < Integer.MIN_VALUE) {
        return JqValue.fromBoolean(false);
      }
      int index = (int) numericKey;
      if (index < 0) {
        return JqValue.fromBoolean(false);
      }
      int length = (int) container.length().asNumber();
      boolean exists = index < length;
      return JqValue.fromBoolean(exists);
    }

    throw new RuntimeException(
        "Cannot check whether " + containerType + " has a " + keyType + " key");
  }
}
