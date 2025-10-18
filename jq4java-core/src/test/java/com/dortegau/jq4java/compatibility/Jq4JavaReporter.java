package com.dortegau.jq4java.compatibility;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Reporter for jq4java test results with aggregated and granular failure analysis.
 */
public class Jq4JavaReporter {

    // ANSI color codes for console output
    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BLUE = "\u001B[34m";
    private static final String CYAN = "\u001B[36m";
    private static final String BOLD = "\u001B[1m";

    private final boolean useColors;

    public Jq4JavaReporter(boolean useColors) {
        this.useColors = useColors;
    }

    public Jq4JavaReporter() {
        this(true); // Default to using colors
    }

    /**
     * Print a comprehensive jq4java test report.
     */
    public void printReport(Jq4JavaReport report) {
        printHeader(report);
        printSummary(report);
        printFailureAnalysis(report);
        printPerformance(report);
        printFooter();
    }

    /**
     * Print just the summary statistics.
     */
    public void printSummary(Jq4JavaReport report) {
        println("");
        println(bold(cyan("=== jq4java TEST SUMMARY ===")));
        println("");

        println(bold("Test Results:"));
        println("  Total tests: " + report.getTotalTests());
        println("  Passed: " + green(String.valueOf(report.getPassedTests())));
        println("  Failed: " + red(String.valueOf(report.getFailedTests())));
        println("  Errors: " + red(String.valueOf(report.getErrorTests())));
        println("  Success rate: " + formatSuccessRate(report.getSuccessRate()));
        println("");
    }

    /**
     * Print detailed failure analysis.
     */
    public void printFailureAnalysis(Jq4JavaReport report) {
        List<TestResult> failures = report.getFailuresAndErrors();

        if (failures.isEmpty()) {
            println(green("✓ Perfect! All tests passed."));
            return;
        }

        println("");
        println(bold(red("=== FAILURE ANALYSIS ===")));
        println("");

        // Aggregate failures by error type
        printAggregatedFailures(failures);
        println("");

        // Granular failure details
        printGranularFailures(failures);
    }

    /**
     * Print aggregated failure statistics by error patterns.
     */
    private void printAggregatedFailures(List<TestResult> failures) {
        println(bold("Failure Categories:"));

        // Group failures by error pattern
        Map<String, List<TestResult>> errorCategories = failures.stream()
            .collect(Collectors.groupingBy(this::categorizeError));

        // Sort by frequency (most common first)
        errorCategories.entrySet().stream()
            .sorted((e1, e2) -> Integer.compare(e2.getValue().size(), e1.getValue().size()))
            .forEach(entry -> {
                String category = entry.getKey();
                int count = entry.getValue().size();
                double percentage = (count * 100.0) / failures.size();

                println(String.format("  %s%s%s: %s%d%s tests (%.1f%%)",
                    bold(""), category, "",
                    red(""), count, "",
                    percentage));
            });
    }

    /**
     * Print granular failure details.
     */
    private void printGranularFailures(List<TestResult> failures) {
        println(bold("Detailed Failures:"));
        println("");

        int maxToShow = Math.min(20, failures.size()); // Show first 20 failures
        for (int i = 0; i < maxToShow; i++) {
            TestResult failure = failures.get(i);
            JqTestCase testCase = failure.getTestCase();

            println(String.format("  %s%d. %s%s", bold(""), i + 1, testCase.getTestId(), ""));
            println("     Program: " + yellow(testCase.getProgram()));

            if (testCase.getInput() != null && !testCase.getInput().trim().isEmpty()) {
                String input = testCase.getInput().trim();
                if (input.length() > 50) {
                    input = input.substring(0, 47) + "...";
                }
                println("     Input: " + input);
            }

            // Always show expected output
            String expectedOutput = testCase.getExpectedOutput();
            if (expectedOutput != null) {
                if (expectedOutput.trim().isEmpty()) {
                    println("     Expected: " + cyan("(empty)"));
                } else {
                    String expected = expectedOutput.trim();
                    if (expected.length() > 50) {
                        expected = expected.substring(0, 47) + "...";
                    }
                    println("     Expected: " + cyan(expected));
                }
            } else if (testCase.shouldFail()) {
                String expectedError = testCase.getExpectedError();
                if (expectedError != null) {
                    String error = expectedError.trim();
                    if (error.length() > 50) {
                        error = error.substring(0, 47) + "...";
                    }
                    println("     Expected Error: " + cyan(error));
                }
            }

            String errorMsg = failure.getErrorMessage();
            if (errorMsg != null && errorMsg.length() > 100) {
                errorMsg = errorMsg.substring(0, 97) + "...";
            }
            println("     Error: " + red(errorMsg));
            println("");
        }

        if (failures.size() > maxToShow) {
            println(String.format("  ... and %d more failures", failures.size() - maxToShow));
            println("");
        }
    }

    /**
     * Categorize error by common patterns.
     */
    private String categorizeError(TestResult result) {
        String error = result.getErrorMessage();
        if (error == null) {
            return "Unknown Error";
        }

        error = error.toLowerCase();

        if (error.contains("parse error") || error.contains("mismatched input")) {
            return "Parse Errors";
        } else if (error.contains("not defined")) {
            return "Missing Functions";
        } else if (error.contains("cannot index")) {
            return "Index Errors";
        } else if (error.contains("output mismatch")) {
            return "Output Mismatch";
        } else if (error.contains("token recognition error")) {
            return "Lexer Errors";
        } else if (error.contains("escape")) {
            return "String Escape Issues";
        } else {
            return "Other Errors";
        }
    }

    /**
     * Print performance statistics.
     */
    private void printPerformance(Jq4JavaReport report) {
        println("");
        println(bold(blue("=== PERFORMANCE ===")));
        println("");

        long totalMs = report.getTotalExecutionTime().toMillis();
        int totalTests = report.getTotalTests();

        println("Total execution time: " + formatDuration(totalMs));
        if (totalTests > 0) {
            double avgMs = (double) totalMs / totalTests;
            println(String.format("Average per test: %.2fms", avgMs));
        }
    }

    private void printHeader(Jq4JavaReport report) {
        println(bold(cyan("===============================================")));
        println(bold(cyan("              jq4java TEST REPORT")));
        println(bold(cyan("===============================================")));
        println("");
        println("Generated: " + report.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        println("jq4java version: " + report.getJq4javaVersion());
    }

    private void printFooter() {
        println("");
        println(bold(cyan("===============================================")));
    }

    private String formatSuccessRate(double rate) {
        String formatted = String.format("%.2f%%", rate);
        if (rate >= 95.0) {
            return green(formatted);
        } else if (rate >= 80.0) {
            return yellow(formatted);
        } else {
            return red(formatted);
        }
    }

    private String formatDuration(long milliseconds) {
        if (milliseconds < 1000) {
            return milliseconds + "ms";
        } else if (milliseconds < 60000) {
            return String.format("%.2fs", milliseconds / 1000.0);
        } else {
            long minutes = milliseconds / 60000;
            long seconds = (milliseconds % 60000) / 1000;
            return minutes + "m " + seconds + "s";
        }
    }

    // Color helper methods
    private String color(String text, String colorCode) {
        return useColors ? colorCode + text + RESET : text;
    }

    private String red(String text) { return color(text, RED); }
    private String green(String text) { return color(text, GREEN); }
    private String yellow(String text) { return color(text, YELLOW); }
    private String blue(String text) { return color(text, BLUE); }
    private String cyan(String text) { return color(text, CYAN); }
    private String bold(String text) { return color(text, BOLD); }

    private void println(String text) {
        System.out.println(text);
    }
}