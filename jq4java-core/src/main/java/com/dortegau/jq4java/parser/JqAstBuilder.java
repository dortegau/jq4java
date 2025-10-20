package com.dortegau.jq4java.parser;

import com.dortegau.jq4java.ast.Alternative;
import com.dortegau.jq4java.ast.And;
import com.dortegau.jq4java.ast.Arithmetic;
import com.dortegau.jq4java.ast.ArrayConstruction;
import com.dortegau.jq4java.ast.ArrayIndexing;
import com.dortegau.jq4java.ast.ArrayIteration;
import com.dortegau.jq4java.ast.ArraySlicing;
import com.dortegau.jq4java.ast.Comma;
import com.dortegau.jq4java.ast.Comparison;
import com.dortegau.jq4java.ast.Conditional;
import com.dortegau.jq4java.ast.Expression;
import com.dortegau.jq4java.ast.FieldAccess;
import com.dortegau.jq4java.ast.FormatFunction;
import com.dortegau.jq4java.ast.Identity;
import com.dortegau.jq4java.ast.In;
import com.dortegau.jq4java.ast.InterpolatedString;
import com.dortegau.jq4java.ast.Literal;
import com.dortegau.jq4java.ast.MapFunction;
import com.dortegau.jq4java.ast.MapValuesFunction;
import com.dortegau.jq4java.ast.Not;
import com.dortegau.jq4java.ast.ObjectConstruction;
import com.dortegau.jq4java.ast.Or;
import com.dortegau.jq4java.ast.Pipe;
import com.dortegau.jq4java.ast.Range;
import com.dortegau.jq4java.ast.Select;
import com.dortegau.jq4java.ast.UnaryMinus;
import com.dortegau.jq4java.ast.UpdatableExpression;
import com.dortegau.jq4java.ast.UpdateAssignment;
import com.dortegau.jq4java.ast.WithEntries;
import com.dortegau.jq4java.ast.ZeroArgFunction;
import com.dortegau.jq4java.json.JqValue;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Visitor implementation that builds AST nodes from the parse tree.
 */
public class JqAstBuilder extends JqGrammarBaseVisitor<Expression> {

  @Override
  public Expression visitProgram(JqGrammarParser.ProgramContext ctx) {
    return visitExpression(ctx.expression());
  }

  @Override
  public Expression visitExpression(JqGrammarParser.ExpressionContext ctx) {
    List<JqGrammarParser.CommaExprContext> commaExprs = ctx.commaExpr();
    if (commaExprs.size() == 1) {
      return visitCommaExpr(commaExprs.get(0));
    }
    Expression result = visitCommaExpr(commaExprs.get(0));
    for (int i = 1; i < commaExprs.size(); i++) {
      result = new Pipe(result, visitCommaExpr(commaExprs.get(i)));
    }
    return result;
  }

  @Override
  public Expression visitCommaExpr(JqGrammarParser.CommaExprContext ctx) {
    List<JqGrammarParser.UpdateExprContext> updateExprs = ctx.updateExpr();
    if (updateExprs.size() == 1) {
      return visit(updateExprs.get(0));
    }
    List<Expression> expressions = new ArrayList<>();
    for (JqGrammarParser.UpdateExprContext updateExpr : updateExprs) {
      expressions.add(visit(updateExpr));
    }
    return new Comma(expressions);
  }

  @Override
  public Expression visitUpdateExpr(JqGrammarParser.UpdateExprContext ctx) {
    List<JqGrammarParser.AlternativeExprContext> altExprs = ctx.alternativeExpr();
    Expression left = visitAlternativeExpr(altExprs.get(0));

    if (altExprs.size() == 1) {
      return left;
    }

    if (!(left instanceof UpdatableExpression)) {
      throw new RuntimeException("Left-hand side of update assignment is not updatable");
    }

    Expression right = visitAlternativeExpr(altExprs.get(1));
    String operator = ctx.getChild(1).getText();
    return new UpdateAssignment((UpdatableExpression) left, operator, right);
  }

  @Override
  public Expression visitAlternativeExpr(JqGrammarParser.AlternativeExprContext ctx) {
    List<JqGrammarParser.ConditionalExprContext> conditionalExprs = ctx.conditionalExpr();
    if (conditionalExprs.size() == 1) {
      return visit(conditionalExprs.get(0));
    }
    Expression result = visit(conditionalExprs.get(0));
    for (int i = 1; i < conditionalExprs.size(); i++) {
      result = new Alternative(result, visit(conditionalExprs.get(i)));
    }
    return result;
  }

  @Override
  public Expression visitConditionalExpression(JqGrammarParser.ConditionalExpressionContext ctx) {
    Expression condition = visit(ctx.expression(0));
    Expression thenExpr = visit(ctx.expression(1));

    List<Conditional.ElifBranch> elifBranches = new ArrayList<>();
    Expression elseExpr = null;

    // Process elif and else clauses
    int expressionIndex = 2;
    for (int i = 0; i < ctx.getChildCount(); i++) {
      if (ctx.getChild(i).getText().equals("elif")) {
        Expression elifCondition = visit(ctx.expression(expressionIndex));
        Expression elifThen = visit(ctx.expression(expressionIndex + 1));
        elifBranches.add(new Conditional.ElifBranch(elifCondition, elifThen));
        expressionIndex += 2;
      } else if (ctx.getChild(i).getText().equals("else")) {
        elseExpr = visit(ctx.expression(expressionIndex));
        break;
      }
    }

    return new Conditional(condition, thenExpr, elifBranches, elseExpr);
  }

  @Override
  public Expression visitNonConditionalExpr(JqGrammarParser.NonConditionalExprContext ctx) {
    return visitLogicalExpr(ctx.logicalExpr());
  }

  @Override
  public Expression visitLogicalExpr(JqGrammarParser.LogicalExprContext ctx) {
    List<JqGrammarParser.ComparisonExprContext> compExprs = ctx.comparisonExpr();
    if (compExprs.size() == 1) {
      return visitComparisonExpr(compExprs.get(0));
    }
    Expression result = visitComparisonExpr(compExprs.get(0));
    for (int i = 1; i < compExprs.size(); i++) {
      String operator = ctx.getChild(i * 2 - 1).getText();
      Expression right = visitComparisonExpr(compExprs.get(i));
      if (operator.equals("and")) {
        result = new And(result, right);
      } else if (operator.equals("or")) {
        result = new Or(result, right);
      }
    }
    return result;
  }

  @Override
  public Expression visitComparisonExpr(JqGrammarParser.ComparisonExprContext ctx) {
    List<JqGrammarParser.ArithmeticExprContext> arithmeticExprs = ctx.arithmeticExpr();
    if (arithmeticExprs.size() == 1) {
      return visitArithmeticExpr(arithmeticExprs.get(0));
    }
    Expression result = visitArithmeticExpr(arithmeticExprs.get(0));
    for (int i = 1; i < arithmeticExprs.size(); i++) {
      String operator = ctx.getChild(i * 2 - 1).getText();
      result = new Comparison(result, operator, visitArithmeticExpr(arithmeticExprs.get(i)));
    }
    return result;
  }

  @Override
  public Expression visitArithmeticExpr(JqGrammarParser.ArithmeticExprContext ctx) {
    List<JqGrammarParser.PostfixContext> postfixes = ctx.postfix();
    if (postfixes.size() == 1) {
      return visit(postfixes.get(0));
    }
    Expression result = visit(postfixes.get(0));
    for (int i = 1; i < postfixes.size(); i++) {
      String operator = ctx.getChild(i * 2 - 1).getText();
      result = new Arithmetic(result, operator, visit(postfixes.get(i)));
    }
    return result;
  }

  @Override
  public Expression visitArrayIterationExpr(JqGrammarParser.ArrayIterationExprContext ctx) {
    Expression base = visit(ctx.postfix());
    return new ArrayIteration(base);
  }

  @Override
  public Expression visitArraySliceExpr(JqGrammarParser.ArraySliceExprContext ctx) {
    Expression base = visit(ctx.postfix());
    JqGrammarParser.SliceContext slice = ctx.slice();
    
    Integer start = null;
    Integer end = null;
    
    if (slice.start != null) {
      Expression startExpr = visit(slice.start);
      if (startExpr instanceof Literal) {
        start = Integer.parseInt(((Literal) startExpr).getValue());
      }
    }
    
    if (slice.end != null) {
      Expression endExpr = visit(slice.end);
      if (endExpr instanceof Literal) {
        end = Integer.parseInt(((Literal) endExpr).getValue());
      }
    }
    
    return new ArraySlicing(start, end, base);
  }

  @Override
  public Expression visitArrayIndexExpr(JqGrammarParser.ArrayIndexExprContext ctx) {
    Expression base = visit(ctx.postfix());
    Expression indexExpr = visit(ctx.expression());
    
    if (indexExpr instanceof Literal) {
      String value = ((Literal) indexExpr).getValue();
      if (value.startsWith("\"")) {
        String fieldName = unquoteString(value);
        return new FieldAccess(fieldName, base);
      } else {
        int index = Integer.parseInt(value);
        return new ArrayIndexing(index, base);
      }
    }
    
    throw new RuntimeException("Array/object index must be a literal");
  }

  @Override
  public Expression visitFieldAccessExpr(JqGrammarParser.FieldAccessExprContext ctx) {
    Expression base = visit(ctx.postfix());
    String fieldName = ctx.IDENTIFIER().getText();
    return new FieldAccess(fieldName, base);
  }

  @Override
  public Expression visitFieldAccessStringExpr(JqGrammarParser.FieldAccessStringExprContext ctx) {
    Expression base = visit(ctx.postfix());
    String fieldName = unquoteString(ctx.STRING().getText());
    return new FieldAccess(fieldName, base);
  }

  @Override
  public Expression visitRootFieldAccess(JqGrammarParser.RootFieldAccessContext ctx) {
    String fieldName = ctx.IDENTIFIER().getText();
    return new FieldAccess(fieldName, new Identity());
  }

  @Override
  public Expression visitRootFieldAccessString(JqGrammarParser.RootFieldAccessStringContext ctx) {
    String fieldName = unquoteString(ctx.STRING().getText());
    return new FieldAccess(fieldName, new Identity());
  }

  private String unquoteString(String quoted) {
    return JqValue.literal(quoted).asString();
  }

  @Override
  public Expression visitRootArrayIteration(JqGrammarParser.RootArrayIterationContext ctx) {
    return new ArrayIteration(new Identity());
  }

  @Override
  public Expression visitRootArraySlice(JqGrammarParser.RootArraySliceContext ctx) {
    JqGrammarParser.SliceContext slice = ctx.slice();
    Integer start = null;
    Integer end = null;
    
    if (slice.start != null) {
      Expression startExpr = visit(slice.start);
      if (startExpr instanceof Literal) {
        start = Integer.parseInt(((Literal) startExpr).getValue());
      }
    }
    
    if (slice.end != null) {
      Expression endExpr = visit(slice.end);
      if (endExpr instanceof Literal) {
        end = Integer.parseInt(((Literal) endExpr).getValue());
      }
    }
    
    return new ArraySlicing(start, end, new Identity());
  }

  @Override
  public Expression visitRootArrayIndex(JqGrammarParser.RootArrayIndexContext ctx) {
    Expression indexExpr = visit(ctx.expression());
    
    if (indexExpr instanceof Literal) {
      String value = ((Literal) indexExpr).getValue();
      if (value.startsWith("\"")) {
        String fieldName = unquoteString(value);
        return new FieldAccess(fieldName, new Identity());
      } else {
        int index = Integer.parseInt(value);
        return new ArrayIndexing(index, new Identity());
      }
    }
    
    throw new RuntimeException("Array/object index must be a literal");
  }

  @Override
  public Expression visitUnaryExprWrapper(JqGrammarParser.UnaryExprWrapperContext ctx) {
    return visit(ctx.unaryExpr());
  }

  @Override
  public Expression visitUnaryMinusExpr(JqGrammarParser.UnaryMinusExprContext ctx) {
    Expression operand = visit(ctx.postfix());
    return new UnaryMinus(operand);
  }

  @Override
  public Expression visitPrimaryExpr(JqGrammarParser.PrimaryExprContext ctx) {
    return visit(ctx.primary());
  }

  @Override
  public Expression visitIdentityExpr(JqGrammarParser.IdentityExprContext ctx) {
    return new Identity();
  }

  @Override
  public Expression visitTrueLiteral(JqGrammarParser.TrueLiteralContext ctx) {
    return new Literal("true");
  }

  @Override
  public Expression visitFalseLiteral(JqGrammarParser.FalseLiteralContext ctx) {
    return new Literal("false");
  }

  @Override
  public Expression visitNullLiteral(JqGrammarParser.NullLiteralContext ctx) {
    return new Literal("null");
  }

  @Override
  public Expression visitNumberLiteral(JqGrammarParser.NumberLiteralContext ctx) {
    return new Literal(ctx.NUMBER().getText());
  }

  @Override
  public Expression visitStringLiteral(JqGrammarParser.StringLiteralContext ctx) {
    String raw = ctx.STRING().getText();
    InterpolatedParts parts = parseInterpolatedString(raw);
    if (!parts.hasInterpolation()) {
      return new Literal(raw);
    }
    return new InterpolatedString(parts.literalParts(), parts.expressions());
  }

  private static class InterpolatedParts {
    private final List<String> literalParts;
    private final List<Expression> expressions;

    InterpolatedParts(List<String> literalParts, List<Expression> expressions) {
      this.literalParts = literalParts;
      this.expressions = expressions;
    }

    boolean hasInterpolation() {
      return !expressions.isEmpty();
    }

    List<String> literalParts() {
      return literalParts;
    }

    List<Expression> expressions() {
      return expressions;
    }
  }

  private InterpolatedParts parseInterpolatedString(String raw) {
    List<String> literalParts = new ArrayList<>();
    List<Expression> expressions = new ArrayList<>();
    StringBuilder current = new StringBuilder();

    int length = raw.length();
    for (int i = 1; i < length - 1; i++) {
      char ch = raw.charAt(i);
      if (ch == '\\') {
        if (i + 1 >= length - 1) {
          throw new RuntimeException("Unterminated escape sequence in string literal");
        }
        char next = raw.charAt(i + 1);
        if (next == '(') {
          literalParts.add(current.toString());
          current.setLength(0);
          int exprStart = i + 2;
          int exprEnd = findInterpolationEnd(raw, exprStart);
          String expressionSource = raw.substring(exprStart, exprEnd);
          expressions.add(JqParser.parse(expressionSource));
          i = exprEnd;
        } else {
          current.append(decodeEscapeSequence(raw, i + 1));
          i += next == 'u' ? 5 : 1; // skip processed escape characters
        }
      } else {
        current.append(ch);
      }
    }

    literalParts.add(current.toString());
    return new InterpolatedParts(literalParts, expressions);
  }

  private String decodeEscapeSequence(String raw, int escapeStart) {
    char escapeType = raw.charAt(escapeStart);
    switch (escapeType) {
      case '"':
        return "\"";
      case '\\':
        return "\\";
      case '/':
        return "/";
      case 'b':
        return "\b";
      case 'f':
        return "\f";
      case 'n':
        return "\n";
      case 'r':
        return "\r";
      case 't':
        return "\t";
      case 'u':
        if (escapeStart + 4 >= raw.length()) {
          throw new RuntimeException("Invalid unicode escape in string literal");
        }
        int codePoint = Integer.parseInt(raw.substring(escapeStart + 1, escapeStart + 5), 16);
        return new String(Character.toChars(codePoint));
      default:
        return String.valueOf(escapeType);
    }
  }

  private int findInterpolationEnd(String raw, int startIndex) {
    int depth = 1;
    boolean inString = false;
    for (int i = startIndex; i < raw.length() - 1; i++) {
      char ch = raw.charAt(i);
      if (inString) {
        if (ch == '\\') {
          i++;
        } else if (ch == '"') {
          inString = false;
        }
      } else {
        if (ch == '"') {
          inString = true;
        } else if (ch == '(') {
          depth++;
        } else if (ch == ')') {
          depth--;
          if (depth == 0) {
            return i;
          }
        }
      }
    }
    throw new RuntimeException("Unterminated interpolation expression in string literal");
  }


  @Override
  public Expression visitNotExpr(JqGrammarParser.NotExprContext ctx) {
    return new Not();
  }



  @Override
  public Expression visitFunctionCall(JqGrammarParser.FunctionCallContext ctx) {
    String functionName = ctx.IDENTIFIER().getText();
    List<Expression> arguments = new ArrayList<>();

    // Collect all arguments (first one plus any after semicolons)
    for (JqGrammarParser.ExpressionContext exprCtx : ctx.expression()) {
      arguments.add(visit(exprCtx));
    }

    switch (functionName) {
      case "map":
        if (arguments.size() != 1) {
          throw new RuntimeException("map/" + arguments.size() + " is not defined");
        }
        return new MapFunction(arguments.get(0));
      case "map_values":
        if (arguments.size() != 1) {
          throw new RuntimeException("map_values/" + arguments.size() + " is not defined");
        }
        return new MapValuesFunction(arguments.get(0));
      case "with_entries":
        if (arguments.size() != 1) {
          throw new RuntimeException("with_entries/" + arguments.size() + " is not defined");
        }
        return new WithEntries(arguments.get(0));
      case "select":
        if (arguments.size() != 1) {
          throw new RuntimeException("select/" + arguments.size() + " is not defined");
        }
        return new Select(arguments.get(0));
      case "in":
        if (arguments.size() != 1) {
          throw new RuntimeException("in/" + arguments.size() + " is not defined");
        }
        return new In(arguments.get(0));
      case "range":
        return new Range(arguments);
      default:
        throw new RuntimeException("Unknown function: " + functionName);
    }
  }

  @Override
  public Expression visitZeroArgFunction(JqGrammarParser.ZeroArgFunctionContext ctx) {
    String functionName = ctx.IDENTIFIER().getText();
    return new ZeroArgFunction(functionName);
  }

  @Override
  public Expression visitFormatFunction(JqGrammarParser.FormatFunctionContext ctx) {
    String formatName = ctx.IDENTIFIER().getText();
    return new FormatFunction(formatName);
  }

  @Override
  public Expression visitArrayConstructor(JqGrammarParser.ArrayConstructorContext ctx) {
    List<Expression> elements = new ArrayList<>();
    for (JqGrammarParser.ExpressionContext exprCtx : ctx.expression()) {
      elements.add(visit(exprCtx));
    }
    return new ArrayConstruction(elements);
  }

  @Override
  public Expression visitObjectConstructor(JqGrammarParser.ObjectConstructorContext ctx) {
    Map<String, Expression> fields = new LinkedHashMap<>();
    for (JqGrammarParser.ObjectFieldContext fieldCtx : ctx.objectField()) {
      String key;
      Expression value;

      if (fieldCtx instanceof JqGrammarParser.ExplicitFieldContext) {
        JqGrammarParser.ExplicitFieldContext explicitCtx =
            (JqGrammarParser.ExplicitFieldContext) fieldCtx;
        key = explicitCtx.IDENTIFIER().getText();
        value = visit(explicitCtx.expression());
      } else if (fieldCtx instanceof JqGrammarParser.StringFieldContext) {
        JqGrammarParser.StringFieldContext stringCtx =
            (JqGrammarParser.StringFieldContext) fieldCtx;
        key = unquoteString(stringCtx.STRING().getText());
        value = visit(stringCtx.expression());
      } else if (fieldCtx instanceof JqGrammarParser.ShorthandFieldContext) {
        JqGrammarParser.ShorthandFieldContext shorthandCtx =
            (JqGrammarParser.ShorthandFieldContext) fieldCtx;
        key = shorthandCtx.IDENTIFIER().getText();
        value = new FieldAccess(key, new Identity());
      } else {
        throw new RuntimeException(
            "Unknown object field type: " + fieldCtx.getClass().getName());
      }

      fields.put(key, value);
    }
    return new ObjectConstruction(fields);
  }

  @Override
  public Expression visitParenExpr(JqGrammarParser.ParenExprContext ctx) {
    return visit(ctx.expression());
  }
}
