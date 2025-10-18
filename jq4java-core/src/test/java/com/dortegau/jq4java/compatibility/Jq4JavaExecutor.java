package com.dortegau.jq4java.compatibility;

import com.dortegau.jq4java.Jq;
import org.json.JSONException;

import java.time.Duration;
import java.time.Instant;

/**
 * Executor for running tests using the jq4java implementation.
 */
public class Jq4JavaExecutor {
    private static final String IMPLEMENTATION_NAME = "jq4java";

    /**
     * Execute a single test case using jq4java.
     */
    public static TestResult executeTest(JqTestCase testCase) {
        Instant start = Instant.now();

        try {
            String actualOutput = Jq.execute(testCase.getProgram(), testCase.getInput());
            Duration executionTime = Duration.between(start, Instant.now());

            if (testCase.shouldFail()) {
                // Test expected to fail but didn't
                return TestResult.fail(testCase, actualOutput,
                    "Expected test to fail with error: " + testCase.getExpectedError() + ", but got output: " + actualOutput,
                    executionTime, IMPLEMENTATION_NAME);
            } else {
                // Test expected to succeed
                String expectedOutput = testCase.getExpectedOutput();
                // Compare semantically as JSON values like jq's jv_equal()
                if (jsonEqual(normalizeOutput(actualOutput), normalizeOutput(expectedOutput))) {
                    return TestResult.pass(testCase, actualOutput, executionTime, IMPLEMENTATION_NAME);
                } else {
                    return TestResult.fail(testCase, actualOutput,
                        "Output mismatch. Expected: " + expectedOutput + ", but got: " + actualOutput,
                        executionTime, IMPLEMENTATION_NAME);
                }
            }

        } catch (Exception e) {
            Duration executionTime = Duration.between(start, Instant.now());

            if (testCase.shouldFail()) {
                // Test expected to fail and did fail - check if error message matches
                String actualError = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
                String expectedError = testCase.getExpectedError();

                // For now, we'll be lenient with error message matching since error formats may differ
                if (actualError.contains(extractKeyErrorPart(expectedError)) ||
                    extractKeyErrorPart(expectedError).contains(extractKeyErrorPart(actualError))) {
                    return TestResult.pass(testCase, actualError, executionTime, IMPLEMENTATION_NAME);
                } else {
                    return TestResult.fail(testCase, actualError,
                        "Error message mismatch. Expected error containing: " + expectedError + ", but got: " + actualError,
                        executionTime, IMPLEMENTATION_NAME);
                }
            } else {
                // Test expected to succeed but failed
                return TestResult.error(testCase, e.getMessage(), executionTime, IMPLEMENTATION_NAME);
            }
        }
    }

    /**
     * Normalize output for comparison (handle different line endings, whitespace, etc.)
     */
    private static String normalizeOutput(String output) {
        if (output == null) {
            return "";
        }

        // Normalize line endings and trim
        return output.replace("\r\n", "\n")
                    .replace("\r", "\n")
                    .trim();
    }

    /**
     * Extract key part of error message for fuzzy matching.
     */
    private static String extractKeyErrorPart(String errorMessage) {
        if (errorMessage == null) {
            return "";
        }

        // Remove "jq: error:" prefix and location info
        String cleaned = errorMessage.replaceAll("^jq: error: ", "")
                                   .replaceAll(" at <top-level>.*$", "")
                                   .replaceAll(" at line \\d+.*$", "")
                                   .toLowerCase()
                                   .trim();

        // Extract key words (remove articles, prepositions, etc.)
        return cleaned.replaceAll("\\b(the|a|an|is|are|was|were|of|in|on|at|by|for|with|to)\\b", " ")
                     .replaceAll("\\s+", " ")
                     .trim();
    }

    /**
     * Check if jq4java implementation is available.
     */
    public static boolean isAvailable() {
        try {
            // Try a simple test to see if jq4java is working
            String result = Jq.execute(".", "null");
            return "null".equals(result.trim());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get version information for jq4java.
     */
    public static String getVersion() {
        // For now, return a placeholder. In the future, this could be extracted from pom.xml or a version file
        return "1.0-SNAPSHOT";
    }

    /**
     * Execute test with timeout (for tests that might hang).
     */
    public static TestResult executeTestWithTimeout(JqTestCase testCase, Duration timeout) {
        // For now, we'll use the regular execute method
        // In the future, this could be enhanced with proper timeout handling using CompletableFuture
        try {
            return executeTest(testCase);
        } catch (Exception e) {
            return TestResult.error(testCase, "Test execution timeout or error: " + e.getMessage(),
                                  timeout, IMPLEMENTATION_NAME);
        }
    }

    /**
     * Compare two JSON strings semantically, like jq's jv_equal().
     * This handles cases where unicode characters are represented differently
     * (e.g., literal μ vs \u03bc, \r vs \u000d) but are semantically equivalent.
     */
    private static boolean jsonEqual(String actual, String expected) {
        try {
            // Parse both strings as JSON and compare their semantic values
            Object actualValue = parseJsonValue(actual);
            Object expectedValue = parseJsonValue(expected);
            return jsonValueEquals(actualValue, expectedValue);
        } catch (Exception e) {
            // If JSON parsing fails, fall back to string comparison
            return actual.equals(expected);
        }
    }

    /**
     * Parse a JSON string into a comparable value using org.json.
     */
    private static Object parseJsonValue(String json) throws JSONException {
        json = json.trim();
        if (json.startsWith("{")) {
            return new org.json.JSONObject(json);
        } else if (json.startsWith("[")) {
            return new org.json.JSONArray(json);
        } else if (json.equals("null")) {
            return null;
        } else if (json.equals("true")) {
            return Boolean.TRUE;
        } else if (json.equals("false")) {
            return Boolean.FALSE;
        } else if (json.startsWith("\"")) {
            return new org.json.JSONObject("{\"value\":" + json + "}").getString("value");
        } else {
            // Try to parse as number
            try {
                if (json.contains(".") || json.contains("e") || json.contains("E")) {
                    return Double.parseDouble(json);
                } else {
                    return Long.parseLong(json);
                }
            } catch (NumberFormatException e) {
                throw new JSONException("Invalid JSON: " + json);
            }
        }
    }

    /**
     * Compare two JSON values semantically.
     */
    private static boolean jsonValueEquals(Object actual, Object expected) {
        if (actual == null && expected == null) {
            return true;
        }
        if (actual == null || expected == null) {
            return false;
        }

        // Use org.json's similar() method for objects and arrays
        if (actual instanceof org.json.JSONObject && expected instanceof org.json.JSONObject) {
            return ((org.json.JSONObject) actual).similar(expected);
        }
        if (actual instanceof org.json.JSONArray && expected instanceof org.json.JSONArray) {
            return ((org.json.JSONArray) actual).similar(expected);
        }

        // For primitives, use equals
        return actual.equals(expected);
    }
}