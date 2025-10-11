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
  private static final JqValue TRUE = new OrgJsonValue(true);
  private static final JqValue FALSE = new OrgJsonValue(false);
  private static final JqValue NULL = new OrgJsonValue(JSONObject.NULL);
  
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
    return NULL;
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
        return NULL;
      case "true":
        return TRUE;
      case "false":
        return FALSE;
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
    Map<String, Object> rawFields = new java.util.LinkedHashMap<>();
    for (Map.Entry<String, JqValue> entry : fields.entrySet()) {
      String key = entry.getKey();
      JqValue val = entry.getValue();
      if (val instanceof OrgJsonValue) {
        rawFields.put(key, ((OrgJsonValue) val).value);
      }
    }
    return new OrgJsonValue(new OrderedJSONObject(rawFields));
  }

  private static class OrderedJSONObject extends JSONObject {
    private final Map<String, Object> orderedMap;

    OrderedJSONObject(Map<String, Object> map) {
      super(map);
      this.orderedMap = new java.util.LinkedHashMap<>(map);
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("{");
      boolean first = true;
      for (Map.Entry<String, Object> entry : orderedMap.entrySet()) {
        if (!first) {
          sb.append(",");
        }
        first = false;
        sb.append("\"").append(entry.getKey()).append("\":");
        Object val = entry.getValue();
        if (val == null || val == JSONObject.NULL) {
          sb.append("null");
        } else if (val instanceof String) {
          sb.append("\"").append(val).append("\"");
        } else {
          sb.append(val.toString());
        }
      }
      sb.append("}");
      return sb.toString();
    }
  }

  public static JqValue fromBoolean(boolean value) {
    return value ? TRUE : FALSE;
  }

  @Override
  public int compareTo(JqValue other) {
    if (!(other instanceof OrgJsonValue)) {
      throw new IllegalArgumentException("Cannot compare with non-OrgJsonValue");
    }
    OrgJsonValue otherValue = (OrgJsonValue) other;
    
    if (value == JSONObject.NULL && otherValue.value == JSONObject.NULL) {
      return 0;
    }
    if (value == JSONObject.NULL) {
      return -1;
    }
    if (otherValue.value == JSONObject.NULL) {
      return 1;
    }
    
    if (value instanceof Number && otherValue.value instanceof Number) {
      double thisNum = ((Number) value).doubleValue();
      double otherNum = ((Number) otherValue.value).doubleValue();
      return Double.compare(thisNum, otherNum);
    }
    
    if (value instanceof String && otherValue.value instanceof String) {
      return ((String) value).compareTo((String) otherValue.value);
    }
    
    throw new RuntimeException("Cannot compare values of different types");
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!(obj instanceof OrgJsonValue)) return false;
    OrgJsonValue other = (OrgJsonValue) obj;
    
    if (value == JSONObject.NULL && other.value == JSONObject.NULL) return true;
    if (value == JSONObject.NULL || other.value == JSONObject.NULL) return false;
    
    if (value instanceof JSONArray && other.value instanceof JSONArray) {
      return value.toString().equals(other.value.toString());
    }
    
    if (value instanceof JSONObject && other.value instanceof JSONObject) {
      return value.toString().equals(other.value.toString());
    }
    
    return value.equals(other.value);
  }

  @Override
  public int hashCode() {
    return value == null ? 0 : value.hashCode();
  }

  @Override
  public JqValue length() {
    if (value == JSONObject.NULL) {
      return new OrgJsonValue(0);
    }
    if (value instanceof JSONArray) {
      return new OrgJsonValue(((JSONArray) value).length());
    }
    if (value instanceof JSONObject) {
      return new OrgJsonValue(((JSONObject) value).length());
    }
    if (value instanceof String) {
      return new OrgJsonValue(((String) value).length());
    }
    if (value instanceof Number) {
      return new OrgJsonValue(Math.abs(((Number) value).intValue()));
    }
    if (value instanceof Boolean) {
      throw new RuntimeException("boolean (" + value + ") has no length");
    }
    throw new RuntimeException("length not supported for type: " + value.getClass());
  }

  @Override
  public JqValue add(JqValue other) {
    if (!(other instanceof OrgJsonValue)) {
      throw new IllegalArgumentException("Cannot add non-OrgJsonValue");
    }
    OrgJsonValue otherValue = (OrgJsonValue) other;

    if (value instanceof Number && otherValue.value instanceof Number) {
      double result = ((Number) value).doubleValue() + ((Number) otherValue.value).doubleValue();
      if (result == (long) result) {
        return new OrgJsonValue((long) result);
      }
      return new OrgJsonValue(result);
    }

    if (value instanceof String && otherValue.value instanceof String) {
      return new OrgJsonValue((String) value + (String) otherValue.value);
    }

    if (value instanceof JSONArray && otherValue.value instanceof JSONArray) {
      JSONArray result = new JSONArray();
      JSONArray arr1 = (JSONArray) value;
      JSONArray arr2 = (JSONArray) otherValue.value;
      for (int i = 0; i < arr1.length(); i++) {
        result.put(arr1.get(i));
      }
      for (int i = 0; i < arr2.length(); i++) {
        result.put(arr2.get(i));
      }
      return new OrgJsonValue(result);
    }

    throw new RuntimeException("Cannot add values of these types");
  }

  @Override
  public JqValue subtract(JqValue other) {
    if (!(other instanceof OrgJsonValue)) {
      throw new IllegalArgumentException("Cannot subtract non-OrgJsonValue");
    }
    OrgJsonValue otherValue = (OrgJsonValue) other;

    if (value instanceof Number && otherValue.value instanceof Number) {
      double result = ((Number) value).doubleValue() - ((Number) otherValue.value).doubleValue();
      if (result == (long) result) {
        return new OrgJsonValue((long) result);
      }
      return new OrgJsonValue(result);
    }

    String type1 = value instanceof String ? "string" : value.getClass().getSimpleName();
    String type2 = otherValue.value instanceof String ? "string" : otherValue.value.getClass().getSimpleName();
    throw new RuntimeException(type1 + " (" + this + ") and " + type2 + " (" + other + ") cannot be subtracted");
  }

  @Override
  public JqValue multiply(JqValue other) {
    if (!(other instanceof OrgJsonValue)) {
      throw new IllegalArgumentException("Cannot multiply non-OrgJsonValue");
    }
    OrgJsonValue otherValue = (OrgJsonValue) other;

    if (value instanceof Number && otherValue.value instanceof Number) {
      double result = ((Number) value).doubleValue() * ((Number) otherValue.value).doubleValue();
      if (result == (long) result) {
        return new OrgJsonValue((long) result);
      }
      return new OrgJsonValue(result);
    }

    throw new RuntimeException("Cannot multiply values of these types");
  }

  @Override
  public JqValue divide(JqValue other) {
    if (!(other instanceof OrgJsonValue)) {
      throw new IllegalArgumentException("Cannot divide non-OrgJsonValue");
    }
    OrgJsonValue otherValue = (OrgJsonValue) other;

    if (value instanceof Number && otherValue.value instanceof Number) {
      double divisor = ((Number) otherValue.value).doubleValue();
      if (divisor == 0) {
        throw new RuntimeException("number (" + value + ") and number (0) cannot be divided because the divisor is zero");
      }
      double result = ((Number) value).doubleValue() / divisor;
      if (result == (long) result) {
        return new OrgJsonValue((long) result);
      }
      return new OrgJsonValue(result);
    }

    throw new RuntimeException("Cannot divide values of these types");
  }

  @Override
  public JqValue modulo(JqValue other) {
    if (!(other instanceof OrgJsonValue)) {
      throw new IllegalArgumentException("Cannot modulo non-OrgJsonValue");
    }
    OrgJsonValue otherValue = (OrgJsonValue) other;

    if (value instanceof Number && otherValue.value instanceof Number) {
      double divisor = ((Number) otherValue.value).doubleValue();
      if (divisor == 0) {
        throw new RuntimeException("number (" + value + ") and number (0) cannot be divided because the divisor is zero");
      }
      double result = ((Number) value).doubleValue() % divisor;
      if (result == (long) result) {
        return new OrgJsonValue((long) result);
      }
      return new OrgJsonValue(result);
    }

    throw new RuntimeException("Cannot modulo values of these types");
  }
}