package com.dortegau.jq4java.ast;

import com.dortegau.jq4java.json.JqValue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * String literal that contains jq-style interpolations.
 */
public class InterpolatedString implements Expression {
  private final List<String> literalParts;
  private final List<Expression> expressions;

  /**
   * Creates a new interpolated string expression.
   *
   * @param literalParts the plain string segments that appear around interpolations
   * @param expressions the embedded expressions, ordered as they appear in the source
   */
  public InterpolatedString(List<String> literalParts, List<Expression> expressions) {
    if (literalParts.isEmpty()) {
      throw new IllegalArgumentException("literalParts must contain at least one entry");
    }
    if (literalParts.size() != expressions.size() + 1) {
      throw new IllegalArgumentException(
          "literalParts must have exactly one more element than expressions");
    }
    this.literalParts = Collections.unmodifiableList(new ArrayList<>(literalParts));
    this.expressions = Collections.unmodifiableList(new ArrayList<>(expressions));
  }

  /** {@inheritDoc} */
  @Override
  public Stream<JqValue> evaluate(JqValue input) {
    return evaluateRecursive(input, 0, literalParts.get(0)).map(JqValue::fromString);
  }

  private Stream<String> evaluateRecursive(JqValue input, int expressionIndex, String prefix) {
    if (expressionIndex >= expressions.size()) {
      return Stream.of(prefix);
    }

    Expression expression = expressions.get(expressionIndex);
    String nextLiteral = literalParts.get(expressionIndex + 1);
    return expression.evaluate(input)
        .flatMap(
            value ->
                evaluateRecursive(
                    input, expressionIndex + 1, prefix + FormatUtils.toText(value) + nextLiteral));
  }

  /**
   * Returns the literal string segments that make up this expression.
   *
   * @return an immutable list of literal string parts
   */
  public List<String> getLiteralParts() {
    return literalParts;
  }

  /**
   * Returns the embedded expressions that will be interpolated at runtime.
   *
   * @return an immutable list of expression segments
   */
  public List<Expression> getExpressions() {
    return expressions;
  }
}
