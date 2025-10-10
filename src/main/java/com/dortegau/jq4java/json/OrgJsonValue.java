package com.dortegau.jq4java.json;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.stream.IntStream;
import java.util.stream.Stream;

public class OrgJsonValue implements JqValue {
    private final Object value;

    private OrgJsonValue(Object value) {
        this.value = value;
    }

    public static JqValue parse(String json) {
        Object parsed = new JSONTokener(json).nextValue();
        return new OrgJsonValue(parsed);
    }

    @Override
    public JqValue get(String field) {
        if (value instanceof JSONObject) {
            JSONObject obj = (JSONObject) value;
            if (obj.has(field)) {
                Object result = obj.get(field);
                return new OrgJsonValue(result);
            }
            return new OrgJsonValue(JSONObject.NULL);
        }
        throw new RuntimeException("Cannot access field on non-object");
    }

    @Override
    public JqValue getIndex(int index) {
        if (value instanceof JSONArray) {
            JSONArray arr = (JSONArray) value;
            int actualIndex = index < 0 ? arr.length() + index : index;
            if (actualIndex >= 0 && actualIndex < arr.length()) {
                return new OrgJsonValue(arr.get(actualIndex));
            }
            return new OrgJsonValue(JSONObject.NULL);
        }
        throw new RuntimeException("Cannot index non-array");
    }

    @Override
    public JqValue slice(Integer start, Integer end) {
        if (value instanceof JSONArray) {
            JSONArray arr = (JSONArray) value;
            int len = arr.length();
            int s = start == null ? 0 : (start < 0 ? len + start : start);
            int e = end == null ? len : (end < 0 ? len + end : end);
            s = Math.max(0, Math.min(s, len));
            e = Math.max(0, Math.min(e, len));
            JSONArray result = new JSONArray();
            for (int i = s; i < e; i++) {
                result.put(arr.get(i));
            }
            return new OrgJsonValue(result);
        }
        throw new RuntimeException("Cannot slice non-array");
    }

    @Override
    public Stream<JqValue> iterate() {
        if (value instanceof JSONArray) {
            JSONArray arr = (JSONArray) value;
            return IntStream.range(0, arr.length())
                .mapToObj(i -> new OrgJsonValue(arr.get(i)));
        }
        if (value instanceof JSONObject) {
            JSONObject obj = (JSONObject) value;
            return obj.keySet().stream()
                .map(key -> new OrgJsonValue(obj.get(key)));
        }
        throw new RuntimeException("Cannot iterate over non-iterable");
    }

    public static JqValue createArray(JqValue[] elements) {
        JSONArray arr = new JSONArray();
        for (JqValue elem : elements) {
            if (elem instanceof OrgJsonValue) {
                arr.put(((OrgJsonValue) elem).value);
            }
        }
        return new OrgJsonValue(arr);
    }
    
    public static JqValue createObject(String[] keys, JqValue[] values) {
        JSONObject obj = new JSONObject();
        for (int i = 0; i < keys.length; i++) {
            if (values[i] instanceof OrgJsonValue) {
                obj.put(keys[i], ((OrgJsonValue) values[i]).value);
            }
        }
        return new OrgJsonValue(obj);
    }

    @Override
    public String toJson() {
        if (value == null || value == JSONObject.NULL) {
            return "null";
        }
        if (value instanceof String) {
            return "\"" + value + "\"";
        }
        return value.toString();
    }
}
