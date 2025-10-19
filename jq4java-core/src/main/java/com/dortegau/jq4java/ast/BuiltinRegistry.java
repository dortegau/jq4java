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
      Class.forName("com.dortegau.jq4java.ast.Select");
      Class.forName("com.dortegau.jq4java.ast.Flatten");
      Class.forName("com.dortegau.jq4java.ast.Add");
      Class.forName("com.dortegau.jq4java.ast.Abs");
      Class.forName("com.dortegau.jq4java.ast.Sort");
      Class.forName("com.dortegau.jq4java.ast.Reverse");
      Class.forName("com.dortegau.jq4java.ast.Unique");
      Class.forName("com.dortegau.jq4java.ast.Transpose");
      Class.forName("com.dortegau.jq4java.ast.Range");
      Class.forName("com.dortegau.jq4java.ast.ToEntries");
      Class.forName("com.dortegau.jq4java.ast.FromEntries");
      Class.forName("com.dortegau.jq4java.ast.WithEntries");
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
