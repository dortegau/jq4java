package com.dortegau.jq4java;

import com.dortegau.jq4java.ast.Expression;
import com.dortegau.jq4java.json.JqValue;
import com.dortegau.jq4java.json.OrgJsonValue;
import com.dortegau.jq4java.parser.Parser;

public class Jq {
    public static String execute(String program, String input) {
        Expression expr = Parser.parse(program);
        JqValue inputValue = OrgJsonValue.parse(input);
        return expr.evaluate(inputValue)
            .map(JqValue::toJson)
            .reduce((a, b) -> a + "\n" + b)
            .orElse("");
    }
}
