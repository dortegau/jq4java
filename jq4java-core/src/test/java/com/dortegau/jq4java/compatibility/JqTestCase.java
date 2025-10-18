package com.dortegau.jq4java.compatibility;

import java.util.Objects;

/**
 * Represents a single test case from the jq reference test suite.
 */
public class JqTestCase {
    private final String program;
    private final String input;
    private final String expectedOutput;
    private final String sourceFile;
    private final int lineNumber;
    private final boolean shouldFail;
    private final String expectedError;

    public JqTestCase(String program, String input, String expectedOutput, String sourceFile, int lineNumber) {
        this(program, input, expectedOutput, sourceFile, lineNumber, false, null);
    }

    public JqTestCase(String program, String input, String expectedError, String sourceFile, int lineNumber, boolean shouldFail) {
        this(program, input, null, sourceFile, lineNumber, shouldFail, expectedError);
    }

    private JqTestCase(String program, String input, String expectedOutput, String sourceFile, int lineNumber, boolean shouldFail, String expectedError) {
        this.program = Objects.requireNonNull(program, "program cannot be null");
        this.input = Objects.requireNonNull(input, "input cannot be null");
        this.expectedOutput = expectedOutput;
        this.sourceFile = Objects.requireNonNull(sourceFile, "sourceFile cannot be null");
        this.lineNumber = lineNumber;
        this.shouldFail = shouldFail;
        this.expectedError = expectedError;
    }

    public String getProgram() {
        return program;
    }

    public String getInput() {
        return input;
    }

    public String getExpectedOutput() {
        return expectedOutput;
    }

    public String getSourceFile() {
        return sourceFile;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public boolean shouldFail() {
        return shouldFail;
    }

    public String getExpectedError() {
        return expectedError;
    }

    public String getTestId() {
        return sourceFile + ":" + lineNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JqTestCase that = (JqTestCase) o;
        return lineNumber == that.lineNumber &&
                shouldFail == that.shouldFail &&
                Objects.equals(program, that.program) &&
                Objects.equals(input, that.input) &&
                Objects.equals(expectedOutput, that.expectedOutput) &&
                Objects.equals(sourceFile, that.sourceFile) &&
                Objects.equals(expectedError, that.expectedError);
    }

    @Override
    public int hashCode() {
        return Objects.hash(program, input, expectedOutput, sourceFile, lineNumber, shouldFail, expectedError);
    }

    @Override
    public String toString() {
        return "JqTestCase{" +
                "program='" + program + '\'' +
                ", input='" + input + '\'' +
                ", sourceFile='" + sourceFile + '\'' +
                ", lineNumber=" + lineNumber +
                ", shouldFail=" + shouldFail +
                '}';
    }
}