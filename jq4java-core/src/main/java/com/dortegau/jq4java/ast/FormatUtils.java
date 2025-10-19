package com.dortegau.jq4java.ast;

import com.dortegau.jq4java.json.JqValue;
import java.util.stream.Collectors;

final class FormatUtils {
  private FormatUtils() {}

  static String toText(JqValue value) {
    return value.isString() ? value.asString() : value.toString();
  }

  static String toJson(JqValue value) {
    return value.toString();
  }

  static String escapeHtml(String text) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < text.length(); i++) {
      char c = text.charAt(i);
      switch (c) {
        case '&':
          sb.append("&amp;");
          break;
        case '<':
          sb.append("&lt;");
          break;
        case '>':
          sb.append("&gt;");
          break;
        case '"':
          sb.append("&quot;");
          break;
        case '\'':
          sb.append("&#39;");
          break;
        default:
          sb.append(c);
      }
    }
    return sb.toString();
  }

  static String formatCsvRow(JqValue array) {
    if (!array.isArray()) {
      throw new RuntimeException(
          "Cannot csv format " + array.typeName() + " (" + array + ")");
    }
    return array.stream()
        .map(FormatUtils::formatCsvField)
        .collect(Collectors.joining(","));
  }

  private static String formatCsvField(JqValue value) {
    if (value.isNull()) {
      return "";
    }
    String typeName = value.typeName();
    if ("array".equals(typeName) || "object".equals(typeName)) {
      throw new RuntimeException(
          "Cannot csv format nested " + typeName + " values: " + value);
    }
    String text = toText(value);
    boolean needsQuoting =
        text.contains(",") || text.contains("\"") || text.contains("\n") || text.contains("\r");
    String escaped = text.replace("\"", "\"\"");
    if (needsQuoting) {
      return "\"" + escaped + "\"";
    }
    return escaped;
  }

  static String formatTsvRow(JqValue array) {
    if (!array.isArray()) {
      throw new RuntimeException(
          "Cannot tsv format " + array.typeName() + " (" + array + ")");
    }
    return array.stream()
        .map(FormatUtils::formatTsvField)
        .collect(Collectors.joining("\t"));
  }

  private static String formatTsvField(JqValue value) {
    if (value.isNull()) {
      return "";
    }
    String typeName = value.typeName();
    if ("array".equals(typeName) || "object".equals(typeName)) {
      throw new RuntimeException(
          "Cannot tsv format nested " + typeName + " values: " + value);
    }
    String text = toText(value);
    String escaped =
        text.replace("\\", "\\\\")
            .replace("\t", "\\t")
            .replace("\r", "\\r")
            .replace("\n", "\\n");
    return escaped;
  }

  static String formatShell(JqValue value) {
    if (value.isArray()) {
      return value.stream().map(FormatUtils::shellQuote).collect(Collectors.joining(" "));
    }
    String type = value.typeName();
    if ("object".equals(type)) {
      throw new RuntimeException("Cannot shell format object values: " + value);
    }
    return shellQuote(value);
  }

  private static String shellQuote(JqValue value) {
    String text = toText(value);
    if (text.isEmpty()) {
      return "''";
    }
    StringBuilder sb = new StringBuilder("'");
    for (int i = 0; i < text.length(); i++) {
      char c = text.charAt(i);
      if (c == '\'') {
        sb.append("'\\''");
      } else {
        sb.append(c);
      }
    }
    sb.append("'");
    return sb.toString();
  }
}
