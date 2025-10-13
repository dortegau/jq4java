package com.dortegau.jq4java.ast;

import com.dortegau.jq4java.json.JqValue;
import java.util.List;
import java.util.stream.Stream;

/**
 * Conditional expression (if-then-else-elif-end): evaluates condition and returns
 * appropriate branch result based on jq truthiness rules.
 */
public class Conditional implements Expression {
  private final Expression condition;
  private final Expression thenExpr;
  private final List<ElifBranch> elifBranches;
  private final Expression elseExpr; // nullable

  public static class ElifBranch {
    private final Expression condition;
    private final Expression thenExpr;

    public ElifBranch(Expression condition, Expression thenExpr) {
      this.condition = condition;
      this.thenExpr = thenExpr;
    }

    public Expression getCondition() {
      return condition;
    }

    public Expression getThenExpr() {
      return thenExpr;
    }
  }

  public Conditional(Expression condition, Expression thenExpr,
                    List<ElifBranch> elifBranches, Expression elseExpr) {
    this.condition = condition;
    this.thenExpr = thenExpr;
    this.elifBranches = elifBranches;
    this.elseExpr = elseExpr;
  }

  @Override
  public Stream<JqValue> evaluate(JqValue input) {
    // Evaluate main condition
    Stream<JqValue> conditionResults = condition.evaluate(input);
    return conditionResults.flatMap(condValue -> {
      if (isTruthy(condValue)) {
        return thenExpr.evaluate(input);
      }

      // Check elif branches
      for (ElifBranch elifBranch : elifBranches) {
        Stream<JqValue> elifConditionResults = elifBranch.getCondition().evaluate(input);
        JqValue elifCondValue = elifConditionResults.findFirst().orElse(JqValue.nullValue());
        if (isTruthy(elifCondValue)) {
          return elifBranch.getThenExpr().evaluate(input);
        }
      }

      // If no conditions match, use else branch or pass through input
      if (elseExpr != null) {
        return elseExpr.evaluate(input);
      } else {
        return Stream.of(input);
      }
    });
  }

  /**
   * Implements jq truthiness rules: false and null are falsy, everything else is truthy.
   */
  private boolean isTruthy(JqValue value) {
    String str = value.toString();
    return !str.equals("null") && !str.equals("false");
  }
}