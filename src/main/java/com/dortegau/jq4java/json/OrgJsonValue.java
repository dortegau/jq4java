package com.dortegau.jq4java.json;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * JqValue implementation using org.json library.
 */
public class OrgJsonValue implements JqValue {
  private final Object value;

  public OrgJsonValue(String jsonString) {
    this.value = new JSONTokener(jsonString).nextValue();
  }

  public static JqValue parse(String jsonString) {
    return new OrgJsonValue(jsonString);
  }

  @Override
  public JqValue get(String key) {
    if (value instanceof JSONObject) {
      JSONObject obj = (JSONObject) value;
      if (obj.has(key)) {
        Object result = obj.get(key);
        return new OrgJsonValue(result);
      }
    }
    return nullValue();
  }

  @Override
  public JqValue get(int index) {
    if (value instanceof JSONArray) {
      JSONArray arr = (JSONArray) value;
      int actualIndex = index < 0 ? arr.length() + index : index;
      if (actualIndex >= 0 && actualIndex < arr.length()) {
        return new OrgJsonValue(arr.get(actualIndex));
      }
    }
    return nullValue();
  }

  @Override
  public Stream<JqValue> stream() {
    if (value instanceof JSONArray) {
      JSONArray arr = (JSONArray) value;
      return IntStream.range(0, arr.length())
          .mapToObj(i -> new OrgJsonValue(arr.get(i)));
    }
    return Stream.empty();
  }

  @Override
  public JqValue slice(Integer start, Integer end) {
    if (value instanceof JSONArray) {
      JSONArray arr = (JSONArray) value;
      int len = arr.length();
      
      int actualStart = start == null ? 0 : (start < 0 ? len + start : start);
      int actualEnd = end == null ? len : (end < 0 ? len + end : end);
      
      actualStart = Math.max(0, Math.min(actualStart, len));
      actualEnd = Math.max(0, Math.min(actualEnd, len));
      
      JSONArray result = new JSONArray();
      for (int i = actualStart; i < actualEnd; i++) {
        result.put(arr.get(i));
      }
      return new OrgJsonValue(result);
    }
    return nullValue();
  }

  @Override
  public boolean isArray() {
    return value instanceof JSONArray;
  }

  private OrgJsonValue(Object value) {
    this.value = value;
  }

  private static String formatKey(String key) {
    StringBuilder sb = new StringBuilder();
    for (char c : key.toCharArray()) {
      if (c == '"') {
        sb.append("\\\"");
      } else {
        sb.append(c);
      }
    }
    return sb.toString();
  }

  private static String formatValue(JqValue value) {
    StringBuilder sb = new StringBuilder();
    for (char c : value.toString().toCharArray()) {
      if (c == '"') {
        sb.append("\\\"");
      } else {
        sb.append(c);
      }
    }
    return sb.toString();
  }

  @Override
  public String toString() {
    if (value == JSONObject.NULL) {
      return "null";
    }
    if (value instanceof String) {
      return "\"" + value + "\"";
    }
    return value.toString();
  }

  public static JqValue nullValue() {
    return new OrgJsonValue(JSONObject.NULL);
  }

  /**
   * Creates a JqValue from a literal string value.
   *
   * @param literalValue the literal value as a string
   * @return a new JqValue representing the parsed literal
   */
  public static JqValue literal(String literalValue) {
    switch (literalValue) {
      case "null":
        return nullValue();
      case "true":
        return new OrgJsonValue(true);
      case "false":
        return new OrgJsonValue(false);
      default:
        try {
          if (literalValue.contains(".")) {
            return new OrgJsonValue(Double.parseDouble(literalValue));
          } else {
            return new OrgJsonValue(Integer.parseInt(literalValue));
          }
        } catch (NumberFormatException e) {
          return new OrgJsonValue(literalValue);
        }
    }
  }

  /**
   * Creates a JqValue array from a list of values.
   *
   * @param values the list of values to include in the array
   * @return a new JqValue representing the array
   */
  public static JqValue array(List<JqValue> values) {
    JSONArray arr = new JSONArray();
    for (JqValue val : values) {
      if (val instanceof OrgJsonValue) {
        arr.put(((OrgJsonValue) val).value);
      }
    }
    return new OrgJsonValue(arr);
  }

  /**
   * Creates a JqValue object from a map of key-value pairs.
   *
   * @param fields the map of field names to values
   * @return a new JqValue representing the object
   */
  public static JqValue object(Map<String, JqValue> fields) {
    JSONObject obj = new JSONObject();
    for (Map.Entry<String, JqValue> entry : fields.entrySet()) {
      String key = entry.getKey();
      JqValue val = entry.getValue();
      if (val instanceof OrgJsonValue) {
        obj.put(key, ((OrgJsonValue) val).value);
      }
    }
    return new OrgJsonValue(obj);
  }
}