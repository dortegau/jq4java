package com.dortegau.jq4java.compatibility;

import java.time.Duration;
import java.util.Objects;

/**
 * Represents the result of executing a test case.
 */
public class TestResult {
    public enum Status {
        PASS, FAIL, ERROR, SKIP
    }

    private final JqTestCase testCase;
    private final Status status;
    private final String actualOutput;
    private final String errorMessage;
    private final Duration executionTime;
    private final String implementation; // "jq4java" or "native-jq"

    public TestResult(JqTestCase testCase, Status status, String actualOutput, String errorMessage,
                     Duration executionTime, String implementation) {
        this.testCase = Objects.requireNonNull(testCase, "testCase cannot be null");
        this.status = Objects.requireNonNull(status, "status cannot be null");
        this.actualOutput = actualOutput;
        this.errorMessage = errorMessage;
        this.executionTime = executionTime;
        this.implementation = Objects.requireNonNull(implementation, "implementation cannot be null");
    }

    public static TestResult pass(JqTestCase testCase, String actualOutput, Duration executionTime, String implementation) {
        return new TestResult(testCase, Status.PASS, actualOutput, null, executionTime, implementation);
    }

    public static TestResult fail(JqTestCase testCase, String actualOutput, String errorMessage, Duration executionTime, String implementation) {
        return new TestResult(testCase, Status.FAIL, actualOutput, errorMessage, executionTime, implementation);
    }

    public static TestResult error(JqTestCase testCase, String errorMessage, Duration executionTime, String implementation) {
        return new TestResult(testCase, Status.ERROR, null, errorMessage, executionTime, implementation);
    }

    public static TestResult skip(JqTestCase testCase, String reason, String implementation) {
        return new TestResult(testCase, Status.SKIP, null, reason, Duration.ZERO, implementation);
    }

    public JqTestCase getTestCase() {
        return testCase;
    }

    public Status getStatus() {
        return status;
    }

    public String getActualOutput() {
        return actualOutput;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Duration getExecutionTime() {
        return executionTime;
    }

    public String getImplementation() {
        return implementation;
    }

    public boolean isSuccess() {
        return status == Status.PASS;
    }

    public boolean isFailure() {
        return status == Status.FAIL || status == Status.ERROR;
    }

    public boolean isSkipped() {
        return status == Status.SKIP;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestResult that = (TestResult) o;
        return Objects.equals(testCase, that.testCase) &&
                status == that.status &&
                Objects.equals(actualOutput, that.actualOutput) &&
                Objects.equals(errorMessage, that.errorMessage) &&
                Objects.equals(executionTime, that.executionTime) &&
                Objects.equals(implementation, that.implementation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(testCase, status, actualOutput, errorMessage, executionTime, implementation);
    }

    @Override
    public String toString() {
        return "TestResult{" +
                "testCase=" + testCase.getTestId() +
                ", status=" + status +
                ", implementation='" + implementation + '\'' +
                ", executionTime=" + executionTime.toMillis() + "ms" +
                '}';
    }
}