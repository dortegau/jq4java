package com.dortegau.jq4java.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BuiltinRegistry {
  private static final List<String> BUILTINS = new ArrayList<>();

  static {
    try {
      Class.forName("com.dortegau.jq4java.ast.Length");
      Class.forName("com.dortegau.jq4java.ast.Keys");
      Class.forName("com.dortegau.jq4java.ast.Type");
      Class.forName("com.dortegau.jq4java.ast.MapFunction");
    } catch (ClassNotFoundException e) {
      // Ignore
    }
  }

  public static void register(String name, int arity) {
    String entry = name + "/" + arity;
    if (!BUILTINS.contains(entry)) {
      BUILTINS.add(entry);
    }
  }

  public static List<String> list() {
    return Collections.unmodifiableList(BUILTINS);
  }
}
