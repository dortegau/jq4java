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
    this(parseJson(jsonString));
  }

  private static Object parseJson(String jsonString) {
    JSONTokener tokener = new JSONTokener(jsonString);
    Object parsed = tokener.nextValue();
    char next = tokener.nextClean();
    if (next != 0) {
      throw tokener.syntaxError("Unexpected trailing characters");
    }

    if (parsed instanceof String) {
      String trimmed = jsonString.trim();
      if (trimmed.isEmpty()
          || trimmed.charAt(0) != '"'
          || trimmed.charAt(trimmed.length() - 1) != '"') {
        throw tokener.syntaxError("Invalid JSON string literal");
      }
    }

    return parsed;
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
      return nullValue();
    } else if (value instanceof JSONArray) {
      throw new RuntimeException("Cannot index array with string \"" + key + "\"");
    } else if (value instanceof String) {
      throw new RuntimeException("Cannot index string with string \"" + key + "\"");
    } else if (value instanceof Number) {
      throw new RuntimeException("Cannot index number with string \"" + key + "\"");
    } else if (value == JSONObject.NULL) {
      return nullValue(); // Native jq returns null when accessing field on null
    } else if (value instanceof Boolean) {
      throw new RuntimeException("Cannot index boolean with string \"" + key + "\"");
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

  @Override
  public String toString() {
    if (value == JSONObject.NULL) {
      return "null";
    }
    if (value instanceof String) {
      return "\"" + escapeString((String) value) + "\"";
    }
    if (value instanceof JSONArray) {
      return toCompactString((JSONArray) value);
    }
    return value.toString();
  }

  private static String escapeString(String str) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < str.length(); i++) {
      char c = str.charAt(i);
      switch (c) {
        case '\\': sb.append("\\\\"); break;
        case '"': sb.append("\\\""); break;
        case '\b': sb.append("\\b"); break;
        case '\f': sb.append("\\f"); break;
        case '\n': sb.append("\\n"); break;
        case '\r': sb.append("\\r"); break;
        case '\t': sb.append("\\t"); break;
        default:
          if (c < 0x20) {
            sb.append(String.format("\\u%04x", (int) c));
          } else {
            sb.append(c);
          }
      }
    }
    return sb.toString();
  }

  private String toCompactString(JSONArray arr) {
    StringBuilder sb = new StringBuilder("[");
    for (int i = 0; i < arr.length(); i++) {
      if (i > 0) {
        sb.append(",");
      }
      Object item = arr.get(i);
      if (item == null || item == JSONObject.NULL) {
        sb.append("null");
      } else if (item instanceof String) {
        sb.append("\"").append(escapeString((String) item)).append("\"");
      } else if (item instanceof JSONArray) {
        sb.append(toCompactString((JSONArray) item));
      } else if (item instanceof JSONObject) {
        sb.append(item.toString());
      } else {
        sb.append(item.toString());
      }
    }
    sb.append("]");
    return sb.toString();
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
          if (literalValue.length() >= 2
              && literalValue.charAt(0) == '"'
              && literalValue.charAt(literalValue.length() - 1) == '"') {
            return new OrgJsonValue(literalValue);
          }
          return OrgJsonValue.fromString(literalValue);
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
        sb.append("\"").append(escapeString(entry.getKey())).append("\":");
        Object val = entry.getValue();
        if (val == null || val == JSONObject.NULL) {
          sb.append("null");
        } else if (val instanceof String) {
          sb.append("\"").append(escapeString((String) val)).append("\"");
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

  public static JqValue fromLong(long value) {
    return new OrgJsonValue(value);
  }

  public static JqValue fromDouble(double value) {
    return new OrgJsonValue(value);
  }

  public static JqValue fromString(String value) {
    return new OrgJsonValue((Object) value);
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
    
    if (value instanceof Number && other.value instanceof Number) {
      return ((Number) value).doubleValue() == ((Number) other.value).doubleValue();
    }
    
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
      return OrgJsonValue.fromString((String) value + (String) otherValue.value);
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

  @Override
  public boolean isTruthy() {
    if (value == JSONObject.NULL) {
      return false;
    }
    if (value instanceof Boolean) {
      return (Boolean) value;
    }
    return true;
  }

  @Override
  public JqValue keys() {
    if (value instanceof JSONObject) {
      JSONObject obj = (JSONObject) value;
      JSONArray result = new JSONArray();
      List<String> keys = new java.util.ArrayList<>(obj.keySet());
      java.util.Collections.sort(keys);
      for (String key : keys) {
        result.put(key);
      }
      return new OrgJsonValue(result);
    }
    if (value instanceof JSONArray) {
      JSONArray arr = (JSONArray) value;
      JSONArray result = new JSONArray();
      for (int i = 0; i < arr.length(); i++) {
        result.put(i);
      }
      return new OrgJsonValue(result);
    }
    String type = value == JSONObject.NULL ? "null" : 
                  value instanceof Number ? "number" :
                  value instanceof String ? "string" :
                  value instanceof Boolean ? "boolean" : "unknown";
    throw new RuntimeException(type + " (" + this + ") has no keys");
  }

  @Override
  public JqValue type() {
    String typeName;
    if (value == JSONObject.NULL) {
      typeName = "null";
    } else if (value instanceof Number) {
      typeName = "number";
    } else if (value instanceof String) {
      typeName = "string";
    } else if (value instanceof Boolean) {
      typeName = "boolean";
    } else if (value instanceof JSONArray) {
      typeName = "array";
    } else if (value instanceof JSONObject) {
      typeName = "object";
    } else {
      typeName = "unknown";
    }
    return new OrgJsonValue((Object) typeName);
  }

  @Override
  public boolean isNull() {
    return value == JSONObject.NULL;
  }

  @Override
  public boolean isNumber() {
    return value instanceof Number;
  }

  @Override
  public double asNumber() {
    if (value instanceof Number) {
      return ((Number) value).doubleValue();
    }
    throw new RuntimeException("Value is not a number: " + value);
  }

  @Override
  public boolean isString() {
    return value instanceof String;
  }

  @Override
  public String asString() {
    if (value instanceof String) {
      return (String) value;
    }
    throw new RuntimeException("Value is not a string: " + this);
  }

  @Override
  public JqValue flatten(int depth) {
    if (!isArray()) {
      throw new RuntimeException("Cannot flatten non-array");
    }

    JSONArray arr = (JSONArray) value;
    JSONArray result = new JSONArray();

    for (int i = 0; i < arr.length(); i++) {
      Object item = arr.get(i);
      if (depth > 0 && item instanceof JSONArray) {
        JqValue nestedFlattened = new OrgJsonValue(item).flatten(depth - 1);
        if (nestedFlattened instanceof OrgJsonValue) {
          JSONArray nestedArr = (JSONArray) ((OrgJsonValue) nestedFlattened).value;
          for (int j = 0; j < nestedArr.length(); j++) {
            result.put(nestedArr.get(j));
          }
        }
      } else {
        result.put(item);
      }
    }

    return new OrgJsonValue(result);
  }

  @Override
  public JqValue add() {
    if (!isArray()) {
      throw new RuntimeException("Cannot add elements of non-array");
    }

    JSONArray arr = (JSONArray) value;
    if (arr.length() == 0) {
      return nullValue();
    }

    Object first = arr.get(0);

    // Handle numbers
    if (first instanceof Number) {
      double sum = 0;
      for (int i = 0; i < arr.length(); i++) {
        Object item = arr.get(i);
        if (!(item instanceof Number)) {
          throw new RuntimeException("Cannot add mixed types");
        }
        sum += ((Number) item).doubleValue();
      }
      if (sum == (long) sum) {
        return new OrgJsonValue((long) sum);
      }
      return new OrgJsonValue(sum);
    }

    // Handle strings
    if (first instanceof String) {
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < arr.length(); i++) {
        Object item = arr.get(i);
        if (!(item instanceof String)) {
          throw new RuntimeException("Cannot add mixed types");
        }
        sb.append((String) item);
      }
      return OrgJsonValue.fromString(sb.toString());
    }

    // Handle arrays
    if (first instanceof JSONArray) {
      JSONArray result = new JSONArray();
      for (int i = 0; i < arr.length(); i++) {
        Object item = arr.get(i);
        if (!(item instanceof JSONArray)) {
          throw new RuntimeException("Cannot add mixed types");
        }
        JSONArray subArr = (JSONArray) item;
        for (int j = 0; j < subArr.length(); j++) {
          result.put(subArr.get(j));
        }
      }
      return new OrgJsonValue(result);
    }

    // Handle objects
    if (first instanceof JSONObject) {
      Map<String, Object> result = new java.util.LinkedHashMap<>();
      for (int i = 0; i < arr.length(); i++) {
        Object item = arr.get(i);
        if (!(item instanceof JSONObject)) {
          throw new RuntimeException("Cannot add mixed types");
        }
        JSONObject obj = (JSONObject) item;
        for (String key : obj.keySet()) {
          result.put(key, obj.get(key));
        }
      }
      return new OrgJsonValue(new OrderedJSONObject(result));
    }

    throw new RuntimeException("Cannot add elements of this type");
  }

  @Override
  public JqValue sort() {
    if (!isArray()) {
      throw new RuntimeException("Cannot sort non-array");
    }

    JSONArray arr = (JSONArray) value;
    List<JqValue> items = new java.util.ArrayList<>();

    for (int i = 0; i < arr.length(); i++) {
      items.add(new OrgJsonValue(arr.get(i)));
    }

    items.sort(null); // Use natural ordering (compareTo)

    JSONArray result = new JSONArray();
    for (JqValue item : items) {
      if (item instanceof OrgJsonValue) {
        result.put(((OrgJsonValue) item).value);
      }
    }

    return new OrgJsonValue(result);
  }

  @Override
  public JqValue reverse() {
    if (!isArray()) {
      throw new RuntimeException("Cannot reverse non-array");
    }

    JSONArray arr = (JSONArray) value;
    JSONArray result = new JSONArray();

    for (int i = arr.length() - 1; i >= 0; i--) {
      result.put(arr.get(i));
    }

    return new OrgJsonValue(result);
  }

  @Override
  public JqValue unique() {
    if (!isArray()) {
      throw new RuntimeException("Cannot get unique elements from non-array");
    }

    JSONArray arr = (JSONArray) value;
    List<JqValue> items = new java.util.ArrayList<>();

    for (int i = 0; i < arr.length(); i++) {
      items.add(new OrgJsonValue(arr.get(i)));
    }

    // Sort first to match jq behavior
    items.sort(null);

    // Remove duplicates
    List<JqValue> unique = new java.util.ArrayList<>();
    JqValue prev = null;
    for (JqValue item : items) {
      if (prev == null || !item.equals(prev)) {
        unique.add(item);
      }
      prev = item;
    }

    JSONArray result = new JSONArray();
    for (JqValue item : unique) {
      if (item instanceof OrgJsonValue) {
        result.put(((OrgJsonValue) item).value);
      }
    }

    return new OrgJsonValue(result);
  }

  @Override
  public JqValue transpose() {
    if (!isArray()) {
      throw new RuntimeException("Cannot transpose non-array");
    }

    JSONArray arr = (JSONArray) value;
    if (arr.length() == 0) {
      return new OrgJsonValue(new JSONArray());
    }

    // Find the maximum length of inner arrays
    int maxLength = 0;
    for (int i = 0; i < arr.length(); i++) {
      Object item = arr.get(i);
      if (item instanceof JSONArray) {
        maxLength = Math.max(maxLength, ((JSONArray) item).length());
      }
    }

    JSONArray result = new JSONArray();
    for (int col = 0; col < maxLength; col++) {
      JSONArray column = new JSONArray();
      for (int row = 0; row < arr.length(); row++) {
        Object item = arr.get(row);
        if (item instanceof JSONArray) {
          JSONArray rowArr = (JSONArray) item;
          if (col < rowArr.length()) {
            column.put(rowArr.get(col));
          }
        }
      }
      if (column.length() > 0) {
        result.put(column);
      }
    }

    return new OrgJsonValue(result);
  }

  @Override
  public JqValue abs() {
    if (value instanceof Number) {
      double result = Math.abs(((Number) value).doubleValue());
      if (result == (long) result) {
        return new OrgJsonValue((long) result);
      }
      return new OrgJsonValue(result);
    }

    String type =
        value == JSONObject.NULL ? "null"
            : value instanceof String ? "string"
            : value instanceof Boolean ? "boolean"
            : value instanceof JSONArray ? "array"
            : value instanceof JSONObject ? "object"
            : "unknown";
    throw new RuntimeException(type + " (" + this + ") cannot be used with abs");
  }
}