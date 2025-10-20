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

  JqValue set(String key, JqValue value);

  JqValue set(int index, JqValue value);

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

  JqValue length();

  JqValue utf8ByteLength();

  JqValue add(JqValue other);

  JqValue add();

  JqValue subtract(JqValue other);

  JqValue multiply(JqValue other);

  JqValue divide(JqValue other);

  JqValue modulo(JqValue other);

  boolean isTruthy();

  JqValue keys();

  JqValue type();

  boolean isNull();

  boolean isNumber();

  double asNumber();

  boolean isString();

  String asString();

  /**
   * Returns the type name of this value as a string (without quotes).
   *
   * @return the type name of this JSON value
   */
  default String typeName() {
    String typeJson = type().toString();
    if (typeJson.length() >= 2 && typeJson.startsWith("\"") && typeJson.endsWith("\"")) {
      return typeJson.substring(1, typeJson.length() - 1);
    }
    return typeJson;
  }

  static JqValue fromLong(long value) {
    return OrgJsonValue.fromLong(value);
  }

  static JqValue fromDouble(double value) {
    return OrgJsonValue.fromDouble(value);
  }

  static JqValue fromString(String value) {
    return OrgJsonValue.fromString(value);
  }

  JqValue flatten(int depth);

  JqValue sort();

  JqValue reverse();

  JqValue unique();

  JqValue transpose();

  JqValue abs();
}