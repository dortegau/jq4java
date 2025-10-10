package com.dortegau.jq4java.json;

import java.util.stream.Stream;

public interface JqValue {
    JqValue get(String field);
    JqValue getIndex(int index);
    JqValue slice(Integer start, Integer end);
    Stream<JqValue> iterate();
    String toJson();
    
    static JqValue array(JqValue[] elements) {
        return OrgJsonValue.createArray(elements);
    }
    
    static JqValue object(String[] keys, JqValue[] values) {
        return OrgJsonValue.createObject(keys, values);
    }
}
