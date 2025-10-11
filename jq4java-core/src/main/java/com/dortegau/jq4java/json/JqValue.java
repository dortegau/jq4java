package com.dortegau.jq4java.json;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Interface for JSON values in jq4java.
 */
public interface JqValue extends Comparable<JqValue> {
  JqValue get(String key);

  JqValue get(int index);

  boolean isArray();

  Stream<JqValue> stream();

  default JqValue slice(Integer start, Integer end) {
    throw new UnsupportedOperationException("slice not supported");
  }

  default String toJson() {
    return toString();
  }

  static JqValue nullValue() {
    return OrgJsonValue.nullValue();
  }

  static JqValue literal(String value) {
    return OrgJsonValue.literal(value);
  }

  static JqValue array(List<JqValue> values) {
    return OrgJsonValue.array(values);
  }

  static JqValue object(Map<String, JqValue> fields) {
    return OrgJsonValue.object(fields);
  }

  static JqValue fromBoolean(boolean value) {
    return OrgJsonValue.fromBoolean(value);
  }
}