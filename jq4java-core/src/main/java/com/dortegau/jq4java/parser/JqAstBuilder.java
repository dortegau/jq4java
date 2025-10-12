package com.dortegau.jq4java.parser;

import com.dortegau.jq4java.ast.Alternative;
import com.dortegau.jq4java.ast.And;
import com.dortegau.jq4java.ast.Arithmetic;
import com.dortegau.jq4java.ast.ArrayConstruction;
import com.dortegau.jq4java.ast.ArrayIndexing;
import com.dortegau.jq4java.ast.ArrayIteration;
import com.dortegau.jq4java.ast.ArraySlicing;
import com.dortegau.jq4java.ast.Builtins;
import com.dortegau.jq4java.ast.Comma;
import com.dortegau.jq4java.ast.Comparison;
import com.dortegau.jq4java.ast.Expression;
import com.dortegau.jq4java.ast.FieldAccess;
import com.dortegau.jq4java.ast.Identity;
import com.dortegau.jq4java.ast.Keys;
import com.dortegau.jq4java.ast.Length;
import com.dortegau.jq4java.ast.Literal;
import com.dortegau.jq4java.ast.MapFunction;
import com.dortegau.jq4java.ast.Not;
import com.dortegau.jq4java.ast.ObjectConstruction;
import com.dortegau.jq4java.ast.Or;
import com.dortegau.jq4java.ast.Pipe;
import com.dortegau.jq4java.ast.Select;
import com.dortegau.jq4java.ast.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
    List<JqGrammarParser.AlternativeExprContext> altExprs = ctx.alternativeExpr();
    if (altExprs.size() == 1) {
      return visitAlternativeExpr(altExprs.get(0));
    }
    List<Expression> expressions = new ArrayList<>();
    for (JqGrammarParser.AlternativeExprContext altExpr : altExprs) {
      expressions.add(visitAlternativeExpr(altExpr));
    }
    return new Comma(expressions);
  }

  @Override
  public Expression visitAlternativeExpr(JqGrammarParser.AlternativeExprContext ctx) {
    List<JqGrammarParser.LogicalExprContext> logicalExprs = ctx.logicalExpr();
    if (logicalExprs.size() == 1) {
      return visitLogicalExpr(logicalExprs.get(0));
    }
    Expression result = visitLogicalExpr(logicalExprs.get(0));
    for (int i = 1; i < logicalExprs.size(); i++) {
      result = new Alternative(result, visitLogicalExpr(logicalExprs.get(i)));
    }
    return result;
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
    return quoted.substring(1, quoted.length() - 1);
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
    return new Literal(ctx.STRING().getText());
  }

  @Override
  public Expression visitLengthExpr(JqGrammarParser.LengthExprContext ctx) {
    return new Length();
  }

  @Override
  public Expression visitBuiltinsExpr(JqGrammarParser.BuiltinsExprContext ctx) {
    return new Builtins();
  }

  @Override
  public Expression visitNotExpr(JqGrammarParser.NotExprContext ctx) {
    return new Not();
  }

  @Override
  public Expression visitKeysExpr(JqGrammarParser.KeysExprContext ctx) {
    return new Keys();
  }

  @Override
  public Expression visitTypeExpr(JqGrammarParser.TypeExprContext ctx) {
    return new Type();
  }

  @Override
  public Expression visitFunctionCall(JqGrammarParser.FunctionCallContext ctx) {
    String functionName = ctx.IDENTIFIER().getText();
    Expression arg = visit(ctx.expression());

    switch (functionName) {
      case "map":
        return new MapFunction(arg);
      case "select":
        return new Select(arg);
      default:
        throw new RuntimeException("Unknown function: " + functionName);
    }
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
        JqGrammarParser.ExplicitFieldContext explicitCtx = (JqGrammarParser.ExplicitFieldContext) fieldCtx;
        key = explicitCtx.IDENTIFIER().getText();
        value = visit(explicitCtx.expression());
      } else if (fieldCtx instanceof JqGrammarParser.StringFieldContext) {
        JqGrammarParser.StringFieldContext stringCtx = (JqGrammarParser.StringFieldContext) fieldCtx;
        key = unquoteString(stringCtx.STRING().getText());
        value = visit(stringCtx.expression());
      } else if (fieldCtx instanceof JqGrammarParser.ShorthandFieldContext) {
        JqGrammarParser.ShorthandFieldContext shorthandCtx = (JqGrammarParser.ShorthandFieldContext) fieldCtx;
        key = shorthandCtx.IDENTIFIER().getText();
        value = new FieldAccess(key, new Identity());
      } else {
        throw new RuntimeException("Unknown object field type: " + fieldCtx.getClass().getName());
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
