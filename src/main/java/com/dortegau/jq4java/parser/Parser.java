package com.dortegau.jq4java.parser;

import com.dortegau.jq4java.ast.ArrayConstruction;
import com.dortegau.jq4java.ast.ArrayIndexing;
import com.dortegau.jq4java.ast.ArrayIteration;
import com.dortegau.jq4java.ast.ArraySlicing;
import com.dortegau.jq4java.ast.Expression;
import com.dortegau.jq4java.ast.FieldAccess;
import com.dortegau.jq4java.ast.Identity;
import com.dortegau.jq4java.ast.Literal;
import com.dortegau.jq4java.ast.ObjectConstruction;
import com.dortegau.jq4java.ast.Pipe;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Parser {
    public static Expression parse(String program) {
        program = program.trim();
        
        // Pipe has lower precedence, parse it first
        int pipeIndex = findTopLevelChar(program, '|');
        if (pipeIndex > 0) {
            String left = program.substring(0, pipeIndex).trim();
            String right = program.substring(pipeIndex + 1).trim();
            return new Pipe(parse(left), parse(right));
        }
        
        if (".".equals(program)) {
            return new Identity();
        }
        
        if (".[]".equals(program)) {
            return new ArrayIteration();
        }
        
        if (program.startsWith("[") && program.endsWith("]") && !program.startsWith(".[" )) {
            String content = program.substring(1, program.length() - 1).trim();
            if (content.isEmpty()) {
                return new ArrayConstruction(new ArrayList<>());
            }
            List<Expression> elements = new ArrayList<>();
            for (String part : splitByComma(content)) {
                elements.add(parse(part.trim()));
            }
            return new ArrayConstruction(elements);
        }
        
        if (program.startsWith("{") && program.endsWith("}")) {
            String content = program.substring(1, program.length() - 1).trim();
            Map<String, Expression> fields = new LinkedHashMap<>();
            if (!content.isEmpty()) {
                for (String part : splitByComma(content)) {
                    int colonIndex = part.indexOf(':');
                    String key = part.substring(0, colonIndex).trim();
                    String value = part.substring(colonIndex + 1).trim();
                    fields.put(key, parse(value));
                }
            }
            return new ObjectConstruction(fields);
        }
        
        if (program.startsWith(".[" ) && program.endsWith("]")) {
            String content = program.substring(2, program.length() - 1);
            if (content.contains(":")) {
                String[] parts = content.split(":", -1);
                Integer start = parts[0].isEmpty() ? null : Integer.parseInt(parts[0]);
                Integer end = parts[1].isEmpty() ? null : Integer.parseInt(parts[1]);
                return new ArraySlicing(start, end, new Identity());
            } else {
                int index = Integer.parseInt(content);
                return new ArrayIndexing(index, new Identity());
            }
        }
        
        if (program.startsWith(".") && program.length() > 1) {
            String path = program.substring(1);
            String[] fields = path.split("\\.");
            
            Expression expr = new Identity();
            for (String field : fields) {
                expr = new FieldAccess(field, expr);
            }
            return expr;
        }
        
        // Literals
        switch (program) {
            case "true":
            case "false":
            case "null":
                return new Literal(program);
            default:
                // Assume it's a number
                return new Literal(program);
        }
    }
    
    private static int findTopLevelChar(String s, char target) {
        int depth = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '[' || c == '{') depth++;
            else if (c == ']' || c == '}') depth--;
            else if (c == target && depth == 0) return i;
        }
        return -1;
    }
    
    private static List<String> splitByComma(String s) {
        List<String> result = new ArrayList<>();
        int depth = 0;
        int start = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '[' || c == '{') depth++;
            else if (c == ']' || c == '}') depth--;
            else if (c == ',' && depth == 0) {
                result.add(s.substring(start, i));
                start = i + 1;
            }
        }
        result.add(s.substring(start));
        return result;
    }
}
