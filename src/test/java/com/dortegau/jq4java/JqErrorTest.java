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
}
