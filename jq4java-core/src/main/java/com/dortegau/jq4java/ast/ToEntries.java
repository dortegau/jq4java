package com.dortegau.jq4java.ast;

import com.dortegau.jq4java.json.JqValue;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class ToEntries implements Expression {
  static {
    BuiltinRegistry.register("to_entries", 0);
  }

  @Override
  public Stream<JqValue> evaluate(JqValue input) {
    if (input.isArray()) {
      // For arrays: convert to [{"key": index, "value": element}, ...]
      List<JqValue> entries = new ArrayList<>();

      input.stream().forEach(value -> {
        int index = entries.size(); // Current index
        Map<String, JqValue> entry = new java.util.LinkedHashMap<>();
        entry.put("key", JqValue.fromLong(index));
        entry.put("value", value);
        entries.add(JqValue.object(entry));
      });

      return Stream.of(JqValue.array(entries));
    }

    // Check if it's an object (not null, not array, not primitive)
    if (!input.isNull() && !input.isArray()) {
      try {
        // Try to get keys - this will throw if it's not an object
        JqValue keys = input.keys();
        List<JqValue> entries = new ArrayList<>();

        // Iterate through keys and create key-value pairs
        keys.stream().forEach(keyValue -> {
          String key = keyValue.toString().replace("\"", ""); // Remove quotes from string
          JqValue value = input.get(key);

          Map<String, JqValue> entry = new java.util.LinkedHashMap<>();
          entry.put("key", JqValue.literal("\"" + key + "\""));
          entry.put("value", value);
          entries.add(JqValue.object(entry));
        });

        return Stream.of(JqValue.array(entries));
      } catch (RuntimeException e) {
        // If keys() throws an exception, it means this type has no keys
        String typeName = input.type().toString().replace("\"", "");
        throw new RuntimeException(typeName + " (" + input + ") has no keys");
      }
    }

    // For null and other unsupported types
    String typeName = input.isNull() ? "null" : input.type().toString().replace("\"", "");
    throw new RuntimeException(typeName + " (" + input + ") has no keys");
  }
}