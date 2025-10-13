package com.dortegau.jq4java;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JqErrorTest {
  @Test
  void testInvalidSyntax() {
    RuntimeException ex = assertThrows(RuntimeException.class, 
        () -> Jq.execute("invalid syntax here", "null"));
    assertTrue(ex.getMessage().contains("Parse error"));
  }

  @Test
  void testUnclosedBracket() {
    RuntimeException ex = assertThrows(RuntimeException.class, 
        () -> Jq.execute(".[0", "[1,2,3]"));
    assertTrue(ex.getMessage().contains("Parse error"));
  }

  @Test
  void testInvalidFieldAccess() {
    RuntimeException ex = assertThrows(RuntimeException.class, 
        () -> Jq.execute(".123", "{}"));
    assertTrue(ex.getMessage().contains("Parse error"));
  }

  @Test
  void testMissingExpression() {
    RuntimeException ex = assertThrows(RuntimeException.class, 
        () -> Jq.execute("|", "null"));
    assertTrue(ex.getMessage().contains("Parse error"));
  }

  @Test
  void testUnclosedString() {
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> Jq.execute("\"hello", "null"));
    assertTrue(ex.getMessage().contains("Parse error") || ex.getMessage().contains("token"));
  }

  @Test
  void testUnclosedBracketWithString() {
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> Jq.execute(".[\"foo\"", "{}"));
    assertTrue(ex.getMessage().contains("Parse error"));
  }

  @Test
  void testStringWithoutQuotesInBracket() {
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> Jq.execute(".[foo]", "{}"));
    assertTrue(ex.getMessage().contains("Parse error"));
  }

  @Test
  void testObjectKeyWithoutValue() {
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> Jq.execute("{\"key\":}", "{}"));
    assertTrue(ex.getMessage().contains("Parse error"));
  }

  @Test
  void testObjectTrailingComma() {
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> Jq.execute("{\"a\": 1,}", "{}"));
    assertTrue(ex.getMessage().contains("Parse error"));
  }

  @Test
  void testAlternativeWithoutRight() {
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> Jq.execute(".foo //", "{}"));
    assertTrue(ex.getMessage().contains("Parse error"));
  }

  @Test
  void testAlternativeWithoutLeft() {
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> Jq.execute("// \"default\"", "{}"));
    assertTrue(ex.getMessage().contains("Parse error"));
  }

  @Test
  void testCompareIncompatibleTypes() {
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> Jq.execute("\"hello\" < 5", "null"));
    assertTrue(ex.getMessage().contains("Cannot compare"));
  }

  @Test
  void testCompareArrays() {
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> Jq.execute("[1,2] < [3,4]", "null"));
    assertTrue(ex.getMessage().contains("Cannot compare"));
  }

  @Test
  void testCompareObjects() {
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> Jq.execute("{\"a\":1} < {\"b\":2}", "null"));
    assertTrue(ex.getMessage().contains("Cannot compare"));
  }

  @Test
  void testComparisonWithoutRight() {
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> Jq.execute("5 >", "null"));
    assertTrue(ex.getMessage().contains("Parse error"));
  }

  @Test
  void testComparisonWithoutLeft() {
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> Jq.execute("< 5", "null"));
    assertTrue(ex.getMessage().contains("Parse error"));
  }

  @Test
  void testMultipleComparisonsWithoutParens() {
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> Jq.execute("1 < 2 < 3", "null"));
    assertTrue(ex.getMessage().contains("Parse error") 
        || ex.getMessage().contains("Cannot compare"));
  }

  @Test
  void testLengthOnBoolean() {
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> Jq.execute("true | length", "null"));
    assertTrue(ex.getMessage().contains("has no length"));
  }

  @Test
  void testLengthOnBooleanFalse() {
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> Jq.execute("false | length", "null"));
    assertTrue(ex.getMessage().contains("has no length"));
  }

  @Test
  void testSubtractStrings() {
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> Jq.execute("\"hello\" - \"world\"", "null"));
    assertTrue(ex.getMessage().contains("cannot be subtracted"));
  }

  @Test
  void testDivideByZero() {
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> Jq.execute("5 / 0", "null"));
    assertTrue(ex.getMessage().contains("divisor is zero") || ex.getMessage().contains("divide by zero"));
  }

  @Test
  void testModuloByZero() {
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> Jq.execute("5 % 0", "null"));
    assertTrue(ex.getMessage().contains("divisor is zero") || ex.getMessage().contains("divide by zero"));
  }

  @Test
  void testKeysOnNull() {
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> Jq.execute("null | keys", "null"));
    assertTrue(ex.getMessage().contains("has no keys"));
  }

  @Test
  void testKeysOnNumber() {
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> Jq.execute("5 | keys", "null"));
    assertTrue(ex.getMessage().contains("has no keys"));
  }

  @Test
  void testMapOnNonArray() {
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> Jq.execute("5 | map(. * 2)", "null"));
    assertTrue(ex.getMessage().contains("Cannot iterate"));
  }

  @Test
  void testObjectShorthandOnArray() {
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> Jq.execute("{a}", "[1,2,3]"));
    assertTrue(ex.getMessage().contains("Cannot index array with string"));
  }

  @Test
  void testObjectShorthandOnNumber() {
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> Jq.execute("{a}", "42"));
    assertTrue(ex.getMessage().contains("Cannot index number with string"));
  }

  @Test
  void testObjectShorthandOnString() {
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> Jq.execute("{a}", "\"hello\""));
    assertTrue(ex.getMessage().contains("Cannot index string with string"));
  }

  @Test
  void testObjectMixedShorthandOnArray() {
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> Jq.execute("{a, b: .x}", "[1,2,3]"));
    assertTrue(ex.getMessage().contains("Cannot index array with string"));
  }

  @Test
  void testSelectWithoutArguments() {
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> Jq.execute("select", "null"));
    assertTrue(ex.getMessage().contains("Parse error"));
  }

  @Test
  void testSelectWithInvalidExpression() {
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> Jq.execute("select(.nonexistent > 10)", "42"));
    assertTrue(ex.getMessage().contains("Cannot index number with string"));
  }

  @Test
  void testIncompleteIfStatement() {
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> Jq.execute("if", "null"));
    assertTrue(ex.getMessage().contains("Parse error"));
  }

  @Test
  void testIfWithoutThen() {
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> Jq.execute("if true 3 end", "null"));
    assertTrue(ex.getMessage().contains("Parse error"));
  }

  @Test
  void testIfWithoutEnd() {
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> Jq.execute("if true then 3", "null"));
    assertTrue(ex.getMessage().contains("Parse error"));
  }

  @Test
  void testElifWithoutThen() {
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> Jq.execute("if false then 1 elif true 2 else 3 end", "null"));
    assertTrue(ex.getMessage().contains("Parse error"));
  }

  @Test
  void testElseWithoutEnd() {
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> Jq.execute("if false then 1 else 2", "null"));
    assertTrue(ex.getMessage().contains("Parse error"));
  }
}
