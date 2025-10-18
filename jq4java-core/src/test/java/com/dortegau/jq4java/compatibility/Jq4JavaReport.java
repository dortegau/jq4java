package com.dortegau.jq4java.compatibility;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Report containing only jq4java test results for focused analysis.
 */
public class Jq4JavaReport {
    private final List<TestResult> jq4javaResults;
    private final String jq4javaVersion;
    private final LocalDateTime timestamp;

    public Jq4JavaReport(List<TestResult> jq4javaResults, String jq4javaVersion) {
        this.jq4javaResults = jq4javaResults;
        this.jq4javaVersion = jq4javaVersion;
        this.timestamp = LocalDateTime.now();
    }

    public List<TestResult> getJq4javaResults() {
        return jq4javaResults;
    }

    public String getJq4javaVersion() {
        return jq4javaVersion;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    // Statistics methods
    public int getTotalTests() {
        return jq4javaResults.size();
    }

    public int getPassedTests() {
        return (int) jq4javaResults.stream().filter(TestResult::isSuccess).count();
    }

    public int getFailedTests() {
        return (int) jq4javaResults.stream()
            .filter(r -> r.getStatus() == TestResult.Status.FAIL)
            .count();
    }

    public int getErrorTests() {
        return (int) jq4javaResults.stream()
            .filter(r -> r.getStatus() == TestResult.Status.ERROR)
            .count();
    }

    public double getSuccessRate() {
        if (jq4javaResults.isEmpty()) {
            return 0.0;
        }
        return (getPassedTests() * 100.0) / getTotalTests();
    }

    public Duration getTotalExecutionTime() {
        return jq4javaResults.stream()
            .map(TestResult::getExecutionTime)
            .reduce(Duration.ZERO, Duration::plus);
    }

    public List<TestResult> getFailures() {
        return jq4javaResults.stream()
            .filter(TestResult::isFailure)
            .collect(Collectors.toList());
    }

    public List<TestResult> getErrors() {
        return jq4javaResults.stream()
            .filter(r -> r.getStatus() == TestResult.Status.ERROR)
            .collect(Collectors.toList());
    }

    public List<TestResult> getFailuresAndErrors() {
        return jq4javaResults.stream()
            .filter(TestResult::isFailure)
            .collect(Collectors.toList());
    }
}