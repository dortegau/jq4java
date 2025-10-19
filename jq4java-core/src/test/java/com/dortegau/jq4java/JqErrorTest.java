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
    assertTrue(ex.getMessage().contains("foo/0 is not defined"));
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
  void testAbsOnString() {
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> Jq.execute("abs", "\"hello\""));
    assertTrue(ex.getMessage().contains("cannot be used with abs"));
  }

  @Test
  void testAbsOnArray() {
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> Jq.execute("abs", "[1,2,3]"));
    assertTrue(ex.getMessage().contains("cannot be used with abs"));
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
  void testUriFormatOnNonString() {
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> Jq.execute("@uri", "42"));
    assertTrue(ex.getMessage().contains("Cannot uri encode number"));
  }

  @Test
  void testUriDecodeOnNonString() {
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> Jq.execute("@urid", "42"));
    assertTrue(ex.getMessage().contains("Cannot uri decode number"));
  }

  @Test
  void testCsvFormatOnNonArray() {
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> Jq.execute("@csv", "\"hello\""));
    assertTrue(ex.getMessage().contains("Cannot csv format string"));
  }

  @Test
  void testCsvFormatNestedArray() {
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> Jq.execute("@csv", "[1,[2]]"));
    assertTrue(ex.getMessage().contains("Cannot csv format nested array values"));
  }

  @Test
  void testTsvFormatOnObject() {
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> Jq.execute("@tsv", "{\"a\":1}"));
    assertTrue(ex.getMessage().contains("Cannot tsv format object"));
  }

  @Test
  void testShellFormatOnObject() {
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> Jq.execute("@sh", "{\"a\":1}"));
    assertTrue(ex.getMessage().contains("Cannot shell format object"));
  }

  @Test
  void testUrldecodeWithInvalidPercentSequence() {
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> Jq.execute("@urid", "\"%ZZ\""));
    assertTrue(ex.getMessage().contains("Invalid percent-encoded string"));
  }

  @Test
  void testUriWithoutFormatPrefixIsUndefined() {
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> Jq.execute("uri", "\"hello\""));
    assertTrue(ex.getMessage().contains("uri/0 is not defined"));
  }

  @Test
  void testUridWithoutFormatPrefixIsUndefined() {
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> Jq.execute("urid", "\"hello%20world\""));
    assertTrue(ex.getMessage().contains("urid/0 is not defined"));
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
    assertTrue(ex.getMessage().contains("select/0 is not defined"));
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

  @Test
  void testNestedIfWithoutEnd() {
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> Jq.execute("if true then (if false then 1 else 2", "null"));
    assertTrue(ex.getMessage().contains("Parse error"));
  }

  @Test
  void testNestedIfMissingClosingParen() {
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> Jq.execute("if true then (if false then 1 else 2 end else 3 end", "null"));
    assertTrue(ex.getMessage().contains("Parse error"));
  }

  @Test
  void testNestedIfWithMismatchedParens() {
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> Jq.execute("if true then ((if false then 1 else 2 end) else 3 end", "null"));
    assertTrue(ex.getMessage().contains("Parse error"));
  }

  @Test
  void testIncompleteNestedIf() {
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> Jq.execute("if true then (if", "null"));
    assertTrue(ex.getMessage().contains("Parse error"));
  }

  @Test
  void testNestedIfMissingThen() {
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> Jq.execute("if true then (if false 1 else 2 end) else 3 end", "null"));
    assertTrue(ex.getMessage().contains("Parse error"));
  }

  @Test
  void testDeeplyNestedIncompleteIf() {
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> Jq.execute("if true then (if false then (if true then 1", "null"));
    assertTrue(ex.getMessage().contains("Parse error"));
  }

  @Test
  void testNestedElifWithoutThen() {
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> Jq.execute("if true then (if false then 1 elif true 2 else 3 end) else 4 end", "null"));
    assertTrue(ex.getMessage().contains("Parse error"));
  }

  @Test
  void testNestedIfWithExtraEnd() {
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> Jq.execute("if true then (if false then 1 else 2 end end) else 3 end", "null"));
    assertTrue(ex.getMessage().contains("Parse error"));
  }

  @Test
  void testNestedIfUnbalancedStructure() {
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> Jq.execute("if (if true then false end then 1 else 2 end", "null"));
    assertTrue(ex.getMessage().contains("Parse error"));
  }

  @Test
  void testAddOnNull() {
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> Jq.execute("null | add", "null"));
    assertTrue(ex.getMessage().contains("Cannot iterate over null"));
  }

  @Test
  void testAddOnNumber() {
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> Jq.execute("42 | add", "null"));
    assertTrue(ex.getMessage().contains("Cannot iterate"));
  }

  @Test
  void testSortOnNull() {
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> Jq.execute("null | sort", "null"));
    assertTrue(ex.getMessage().contains("Cannot iterate") || ex.getMessage().contains("Cannot sort"));
  }

  @Test
  void testSortOnObject() {
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> Jq.execute("{\"a\":1} | sort", "null"));
    assertTrue(ex.getMessage().contains("Cannot iterate") || ex.getMessage().contains("Cannot sort"));
  }

  @Test
  void testFlattenOnNull() {
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> Jq.execute("null | flatten", "null"));
    assertTrue(ex.getMessage().contains("Cannot iterate") || ex.getMessage().contains("Cannot flatten"));
  }

  @Test
  void testFlattenOnNumber() {
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> Jq.execute("42 | flatten", "null"));
    assertTrue(ex.getMessage().contains("Cannot iterate") || ex.getMessage().contains("Cannot flatten"));
  }

  @Test
  void testReverseOnNull() {
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> Jq.execute("null | reverse", "null"));
    assertTrue(ex.getMessage().contains("Cannot iterate") || ex.getMessage().contains("Cannot reverse"));
  }

  @Test
  void testReverseOnObject() {
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> Jq.execute("{\"a\":1} | reverse", "null"));
    assertTrue(ex.getMessage().contains("Cannot iterate") || ex.getMessage().contains("Cannot reverse"));
  }

  @Test
  void testUniqueOnNull() {
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> Jq.execute("null | unique", "null"));
    assertTrue(ex.getMessage().contains("Cannot iterate") || ex.getMessage().contains("Cannot get unique"));
  }

  @Test
  void testTransposeOnNull() {
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> Jq.execute("null | transpose", "null"));
    assertTrue(ex.getMessage().contains("Cannot iterate") || ex.getMessage().contains("Cannot transpose"));
  }

  @Test
  void testTransposeOnNumber() {
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> Jq.execute("42 | transpose", "null"));
    assertTrue(ex.getMessage().contains("Cannot iterate") || ex.getMessage().contains("Cannot transpose"));
  }

  @Test
  void testRangeWithZeroStep() {
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> Jq.execute("range(0;5;0)", "null"));
    assertTrue(ex.getMessage().contains("range step cannot be zero"));
  }

  @Test
  void testRangeWithStringArgument() {
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> Jq.execute("range(\"hello\")", "null"));
    assertTrue(ex.getMessage().contains("range argument must be a number"));
  }

  @Test
  void testRangeWithNullArgument() {
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> Jq.execute("range(null)", "null"));
    assertTrue(ex.getMessage().contains("range argument must be a number"));
  }

  @Test
  void testRangeWithArrayArgument() {
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> Jq.execute("range([1,2,3])", "null"));
    assertTrue(ex.getMessage().contains("range argument must be a number"));
  }

  @Test
  void testRangeWithObjectArgument() {
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> Jq.execute("range({\"a\":1})", "null"));
    assertTrue(ex.getMessage().contains("range argument must be a number"));
  }

  @Test
  void testRangeWithBooleanArgument() {
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> Jq.execute("range(true)", "null"));
    assertTrue(ex.getMessage().contains("range argument must be a number"));
  }

  @Test
  void testRangeWithTooManyArguments() {
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> Jq.execute("range(1;2;3;4)", "null"));
    assertTrue(ex.getMessage().contains("range/4 is not defined"));
  }

  @Test
  void testRangeWithNoArguments() {
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> Jq.execute("range", "null"));
    assertTrue(ex.getMessage().contains("range/0 is not defined"));
  }

  @Test
  void testToEntriesOnNull() {
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> Jq.execute("null | to_entries", "null"));
    assertTrue(ex.getMessage().contains("has no keys"));
  }

  @Test
  void testToEntriesOnString() {
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> Jq.execute("\"string\" | to_entries", "null"));
    assertTrue(ex.getMessage().contains("has no keys"));
  }

  @Test
  void testToEntriesOnNumber() {
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> Jq.execute("42 | to_entries", "null"));
    assertTrue(ex.getMessage().contains("has no keys"));
  }

  @Test
  void testToEntriesOnBoolean() {
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> Jq.execute("true | to_entries", "null"));
    assertTrue(ex.getMessage().contains("has no keys"));
  }

  @Test
  void testFromEntriesOnNull() {
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> Jq.execute("null | from_entries", "null"));
    assertTrue(ex.getMessage().contains("Cannot iterate"));
  }

  @Test
  void testFromEntriesOnObject() {
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> Jq.execute("{\"a\":1} | from_entries", "null"));
    assertTrue(ex.getMessage().contains("Cannot iterate"));
  }

  @Test
  void testFromEntriesOnString() {
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> Jq.execute("\"string\" | from_entries", "null"));
    assertTrue(ex.getMessage().contains("Cannot iterate"));
  }

  @Test
  void testFromEntriesOnInvalidArrayElement() {
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> Jq.execute("[\"invalid\"] | from_entries", "null"));
    assertTrue(ex.getMessage().contains("Cannot index string with string"));
  }

  @Test
  void testFromEntriesOnArrayWithNonObjects() {
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> Jq.execute("[1, 2, 3] | from_entries", "null"));
    assertTrue(ex.getMessage().contains("Cannot index number with string"));
  }

  @Test
  void testWithEntriesOnString() {
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> Jq.execute("\"string\" | with_entries(.)", "null"));
    assertTrue(ex.getMessage().contains("has no keys"));
  }

  @Test
  void testWithEntriesOnNumber() {
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> Jq.execute("5 | with_entries(.)", "null"));
    assertTrue(ex.getMessage().contains("has no keys"));
  }

  @Test
  void testWithEntriesWithNonObjectResult() {
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> Jq.execute("{\"a\":1} | with_entries(.value)", "null"));
    assertTrue(ex.getMessage().contains("Cannot index number with string"));
  }

  @Test
  void testWithEntriesMissingKey() {
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> Jq.execute("{\"a\":1} | with_entries({value: .value})", "null"));
    assertTrue(ex.getMessage().contains("Cannot use null (null) as object key"));
  }

  @Test
  void testUnaryMinusOnNull() {
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> Jq.execute("-.", "null"));
    assertTrue(ex.getMessage().contains("cannot be negated") || ex.getMessage().contains("Cannot negate"));
  }

  @Test
  void testUnaryMinusOnString() {
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> Jq.execute("-.", "\"hello\""));
    assertTrue(ex.getMessage().contains("cannot be negated") || ex.getMessage().contains("Cannot negate"));
  }

  @Test
  void testUnaryMinusOnBoolean() {
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> Jq.execute("-.", "true"));
    assertTrue(ex.getMessage().contains("cannot be negated") || ex.getMessage().contains("Cannot negate"));
  }

  @Test
  void testUnaryMinusOnBooleanFalse() {
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> Jq.execute("-.", "false"));
    assertTrue(ex.getMessage().contains("cannot be negated") || ex.getMessage().contains("Cannot negate"));
  }

  @Test
  void testUnaryMinusOnArray() {
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> Jq.execute("-.", "[1,2,3]"));
    assertTrue(ex.getMessage().contains("cannot be negated") || ex.getMessage().contains("Cannot negate"));
  }

  @Test
  void testUnaryMinusOnObject() {
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> Jq.execute("-.", "{\"a\":1}"));
    assertTrue(ex.getMessage().contains("cannot be negated") || ex.getMessage().contains("Cannot negate"));
  }

  @Test
  void testBase64OnObject() {
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> Jq.execute("@base64", "{\"a\":1}"));
    assertTrue(ex.getMessage().contains("Cannot base64 encode"));
  }

  @Test
  void testBase64ArrayOutOfRange() {
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> Jq.execute("@base64", "[256]"));
    assertTrue(ex.getMessage().contains("Cannot base64 encode array containing non-byte value"));
  }

  @Test
  void testBase64DecodeNonString() {
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> Jq.execute("@base64d", "[\"aGVsbG8=\"]"));
    assertTrue(ex.getMessage().contains("Cannot base64 decode"));
  }

  @Test
  void testBase64DecodeInvalidString() {
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> Jq.execute("@base64d", "\"not-base64\""));
    assertTrue(ex.getMessage().contains("Invalid base64"));
  }

  @Test
  void testBase64WithoutFormatPrefixIsUndefined() {
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> Jq.execute("base64", "\"hello\""));
    assertTrue(ex.getMessage().contains("base64/0 is not defined"));
  }

  @Test
  void testBase64DecodeWithoutFormatPrefixIsUndefined() {
    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> Jq.execute("base64d", "\"aGVsbG8=\""));
    assertTrue(ex.getMessage().contains("base64d/0 is not defined"));
  }
}
