package com.dortegau.jq4java.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BuiltinRegistry {
  private static final List<String> BUILTINS = new ArrayList<>();

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
