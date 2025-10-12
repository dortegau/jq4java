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
}
