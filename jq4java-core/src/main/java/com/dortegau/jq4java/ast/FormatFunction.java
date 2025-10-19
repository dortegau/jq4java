package com.dortegau.jq4java.ast;

import com.dortegau.jq4java.json.JqValue;
import java.util.stream.Stream;

/**
 * Represents format functions like @base64, @base64d, @uri, @urid.
 * These functions transform string values according to the specified format.
 */
public class FormatFunction implements Expression {
  private final String formatName;

  /**
   * Creates a new FormatFunction with the specified format name.
   *
   * @param formatName the name of the format (base64, base64d, uri, urid)
   */
  public FormatFunction(String formatName) {
    this.formatName = formatName;
  }

  @Override
  public Stream<JqValue> evaluate(JqValue input) {
    switch (formatName) {
      case "base64":
        return new Base64Encode().evaluate(input);
      case "base64d":
        return new Base64Decode().evaluate(input);
      case "uri":
        return new UriEncode().evaluate(input);
      case "urid":
        return new UriDecode().evaluate(input);
      case "text":
        return new TextFormat().evaluate(input);
      case "json":
        return new JsonFormat().evaluate(input);
      case "html":
        return new HtmlFormat().evaluate(input);
      case "csv":
        return new CsvFormat().evaluate(input);
      case "tsv":
        return new TsvFormat().evaluate(input);
      case "sh":
        return new ShellFormat().evaluate(input);
      default:
        throw new RuntimeException("Unknown format: @" + formatName);
    }
  }
}
