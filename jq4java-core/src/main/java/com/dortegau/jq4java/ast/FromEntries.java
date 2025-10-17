package com.dortegau.jq4java.ast;

import com.dortegau.jq4java.json.JqValue;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

public class FromEntries implements Expression {
  static {
    BuiltinRegistry.register("from_entries", 0);
  }

  @Override
  public Stream<JqValue> evaluate(JqValue input) {
    if (!input.isArray()) {
      throw new RuntimeException("Cannot iterate over " + input.type().toString().replace("\"", "") + " (" + input + ")");
    }

    Map<String, JqValue> resultObject = new LinkedHashMap<>();

    input.stream().forEach(entry -> {
      // Each entry should be an object with "key"/"name" and "value" fields
      JqValue keyValue = null;
      JqValue valueValue = null;

      // Try to get "key" field first
      try {
        keyValue = entry.get("key");
      } catch (RuntimeException e) {
        // If "key" access fails, the entry is not an object
        String typeName = entry.type().toString().replace("\"", "");
        throw new RuntimeException("Cannot index " + typeName + " with string \"key\"");
      }

      // If "key" is null, try "name" field (alternative format)
      if (keyValue.isNull()) {
        try {
          keyValue = entry.get("name");
        } catch (RuntimeException e) {
          // Ignore - stick with null key
        }
      }

      // Check if key is null - this is an error
      if (keyValue.isNull()) {
        throw new RuntimeException("Cannot use null (null) as object key");
      }

      // Get the value field (defaulting to null if missing)
      try {
        valueValue = entry.get("value");
      } catch (RuntimeException e) {
        // If "value" access fails, use null as the value
        valueValue = JqValue.nullValue();
      }

      // Convert key to string (remove quotes if it's a string literal)
      String key = keyValue.toString();
      if (key.startsWith("\"") && key.endsWith("\"")) {
        key = key.substring(1, key.length() - 1);
      }

      resultObject.put(key, valueValue);
    });

    return Stream.of(JqValue.object(resultObject));
  }
}