package com.dortegau.jq4java.compatibility;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Parser for jq reference test files (.test format).
 *
 * The format is:
 * - Comments start with # and are ignored
 * - Blank lines are ignored
 * - Normal tests are groups of 3 lines: program, input, expected_output
 * - Failure tests start with %%FAIL followed by program and expected error message
 */
public class JqReferenceTestParser {

    /**
     * Parse a single test file.
     */
    public static List<JqTestCase> parseFile(Path testFile) throws IOException {
        List<JqTestCase> testCases = new ArrayList<>();
        List<String> lines = Files.readAllLines(testFile);

        String fileName = testFile.getFileName().toString();
        int lineNumber = 0;

        for (int i = 0; i < lines.size(); i++) {
            lineNumber = i + 1;
            String line = lines.get(i).trim();

            // Skip comments and blank lines
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            // Handle failure tests
            if (line.equals("%%FAIL")) {
                if (i + 2 < lines.size()) {
                    String program = lines.get(i + 1).trim();
                    String expectedError = lines.get(i + 2).trim();

                    if (!program.isEmpty() && !program.startsWith("#")) {
                        testCases.add(new JqTestCase(program, "null", expectedError, fileName, lineNumber + 1, true));
                    }
                    i += 2; // Skip the next 2 lines
                    continue;
                }
            }

            // Handle FAIL with IGNORE MSG (we'll parse these too to show what's missing)
            if (line.equals("%%FAIL IGNORE MSG")) {
                if (i + 2 < lines.size()) {
                    String program = lines.get(i + 1).trim();
                    String expectedError = lines.get(i + 2).trim();

                    if (!program.isEmpty() && !program.startsWith("#")) {
                        testCases.add(new JqTestCase(program, "null", expectedError, fileName, lineNumber + 1, true));
                    }
                    i += 2; // Skip the next 2 lines
                    continue;
                }
            }

            // Handle normal tests (program, input, expected_output)
            if (i + 2 < lines.size()) {
                String program = line;
                String input = lines.get(i + 1).trim();
                String expectedOutput = lines.get(i + 2).trim();

                // Skip if any line is a comment
                if (!program.startsWith("#") && !input.startsWith("#") && !expectedOutput.startsWith("#")) {
                    testCases.add(new JqTestCase(program, input, expectedOutput, fileName, lineNumber));
                }

                i += 2; // Skip the next 2 lines
            }
        }

        return testCases;
    }

    /**
     * Parse multiple test files from a directory.
     */
    public static List<JqTestCase> parseDirectory(Path testDirectory) throws IOException {
        List<JqTestCase> allTestCases = new ArrayList<>();

        Files.walk(testDirectory)
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".test"))
                .forEach(testFile -> {
                    try {
                        List<JqTestCase> testCases = parseFile(testFile);
                        allTestCases.addAll(testCases);
                    } catch (IOException e) {
                        System.err.println("Error parsing test file " + testFile + ": " + e.getMessage());
                    }
                });

        return allTestCases;
    }

    /**
     * Parse the main jq.test file.
     */
    public static List<JqTestCase> parseMainTestFile(Path jqReferenceDir) throws IOException {
        Path mainTestFile = jqReferenceDir.resolve("tests").resolve("jq.test");
        if (!Files.exists(mainTestFile)) {
            throw new IOException("Main test file not found: " + mainTestFile);
        }
        return parseFile(mainTestFile);
    }

    /**
     * Count total lines in a test file (for progress reporting).
     */
    public static int countTestsInFile(Path testFile) throws IOException {
        try {
            return parseFile(testFile).size();
        } catch (IOException e) {
            return 0;
        }
    }

    /**
     * Validate that a test case is well-formed.
     */
    public static boolean isValidTestCase(JqTestCase testCase) {
        if (testCase.getProgram() == null || testCase.getProgram().trim().isEmpty()) {
            return false;
        }

        if (testCase.getInput() == null) {
            return false;
        }

        if (testCase.shouldFail()) {
            return testCase.getExpectedError() != null && !testCase.getExpectedError().trim().isEmpty();
        } else {
            return testCase.getExpectedOutput() != null;
        }
    }
}