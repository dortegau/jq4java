package com.dortegau.jq4java.ast;

import com.dortegau.jq4java.json.JqValue;
import java.util.Map;
import java.util.stream.Stream;

public class ObjectConstruction implements Expression {
    private final Map<String, Expression> fields;

    public ObjectConstruction(Map<String, Expression> fields) {
        this.fields = fields;
    }

    @Override
    public Stream<JqValue> evaluate(JqValue value) {
        String[] keys = new String[fields.size()];
        JqValue[] values = new JqValue[fields.size()];
        int i = 0;
        for (Map.Entry<String, Expression> entry : fields.entrySet()) {
            keys[i] = entry.getKey();
            values[i] = entry.getValue().evaluate(value).findFirst().orElse(null);
            i++;
        }
        return Stream.of(JqValue.object(keys, values));
    }
}
