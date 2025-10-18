package com.dortegau.jq4java.compatibility;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Main runner for jq4java tests against jq reference test cases.
 */
public class CompatibilityTestRunner {

    private final Path jqReferenceDir;
    private final Jq4JavaReporter reporter;
    private final boolean verbose;

    public CompatibilityTestRunner(Path jqReferenceDir, boolean verbose) {
        this.jqReferenceDir = jqReferenceDir;
        this.reporter = new Jq4JavaReporter();
        this.verbose = verbose;
    }

    public CompatibilityTestRunner(Path jqReferenceDir) {
        this(jqReferenceDir, false);
    }

    /**
     * Run all jq4java tests and generate a detailed report.
     */
    public Jq4JavaReport runAllTests() throws IOException {
        System.out.println("Starting jq4java test run...");
        System.out.println("jq reference directory: " + jqReferenceDir);

        // Check prerequisites
        checkPrerequisites();

        // Parse test cases
        System.out.println("Parsing test cases from jq reference...");
        List<JqTestCase> testCases = JqReferenceTestParser.parseMainTestFile(jqReferenceDir);
        System.out.println("Found " + testCases.size() + " test cases");

        // Run tests
        List<TestResult> jq4javaResults = runJq4JavaTests(testCases);

        // Generate report
        String jq4javaVersion = Jq4JavaExecutor.getVersion();
        Jq4JavaReport report = new Jq4JavaReport(jq4javaResults, jq4javaVersion);

        // Print report
        reporter.printReport(report);

        return report;
    }

    /**
     * Run a subset of tests (useful for debugging).
     */
    public Jq4JavaReport runTestSubset(int maxTests) throws IOException {
        System.out.println("Starting jq4java test run (max " + maxTests + " tests)...");

        checkPrerequisites();

        List<JqTestCase> allTestCases = JqReferenceTestParser.parseMainTestFile(jqReferenceDir);
        List<JqTestCase> testCases = allTestCases.subList(0, Math.min(maxTests, allTestCases.size()));

        System.out.println("Running " + testCases.size() + " test cases");

        List<TestResult> jq4javaResults = runJq4JavaTests(testCases);

        String jq4javaVersion = Jq4JavaExecutor.getVersion();
        Jq4JavaReport report = new Jq4JavaReport(jq4javaResults, jq4javaVersion);
        reporter.printReport(report);

        return report;
    }

    /**
     * Run only tests that match a specific pattern.
     */
    public Jq4JavaReport runFilteredTests(String programFilter) throws IOException {
        System.out.println("Starting filtered jq4java test run...");
        System.out.println("Filter: programs containing '" + programFilter + "'");

        checkPrerequisites();

        List<JqTestCase> allTestCases = JqReferenceTestParser.parseMainTestFile(jqReferenceDir);
        List<JqTestCase> filteredTestCases = new ArrayList<>();

        for (JqTestCase testCase : allTestCases) {
            if (testCase.getProgram().contains(programFilter)) {
                filteredTestCases.add(testCase);
            }
        }

        System.out.println("Found " + filteredTestCases.size() + " matching test cases");

        List<TestResult> jq4javaResults = runJq4JavaTests(filteredTestCases);

        String jq4javaVersion = Jq4JavaExecutor.getVersion();
        Jq4JavaReport report = new Jq4JavaReport(jq4javaResults, jq4javaVersion);
        reporter.printReport(report);

        return report;
    }

    private void checkPrerequisites() {
        // Check if jq4java is available
        if (!Jq4JavaExecutor.isAvailable()) {
            throw new IllegalStateException("jq4java is not available or not working properly");
        }

        System.out.println("✓ jq4java is available");
    }

    private List<TestResult> runJq4JavaTests(List<JqTestCase> testCases) {
        System.out.println("Running tests with jq4java...");
        List<TestResult> results = new ArrayList<>();
        AtomicInteger completed = new AtomicInteger(0);

        for (JqTestCase testCase : testCases) {
            TestResult result = Jq4JavaExecutor.executeTest(testCase);
            results.add(result);

            int current = completed.incrementAndGet();
            if (verbose || current % 100 == 0) {
                System.out.printf("jq4java: %d/%d tests completed\r", current, testCases.size());
            }
        }

        System.out.println("\nCompleted jq4java tests: " + results.size());
        return results;
    }


    /**
     * Main method for running jq4java tests from command line.
     */
    public static void main(String[] args) {
        try {
            Path jqReferenceDir = null;

            // Parse command line arguments
            boolean verbose = false;
            Integer maxTests = null;
            String filter = null;

            for (int i = 0; i < args.length; i++) {
                switch (args[i]) {
                    case "--verbose":
                    case "-v":
                        verbose = true;
                        break;
                    case "--max-tests":
                        if (i + 1 < args.length) {
                            maxTests = Integer.parseInt(args[++i]);
                        }
                        break;
                    case "--filter":
                        if (i + 1 < args.length) {
                            filter = args[++i];
                        }
                        break;
                    case "--jq-reference-dir":
                        if (i + 1 < args.length) {
                            jqReferenceDir = Paths.get(args[++i]);
                        }
                        break;
                    case "--help":
                    case "-h":
                        printUsage();
                        return;
                }
            }

            // Validate required parameters
            if (jqReferenceDir == null) {
                System.err.println("Error: --jq-reference-dir is required");
                printUsage();
                System.exit(1);
            }

            CompatibilityTestRunner runner = new CompatibilityTestRunner(jqReferenceDir, verbose);

            Jq4JavaReport report;
            if (filter != null) {
                report = runner.runFilteredTests(filter);
            } else if (maxTests != null) {
                report = runner.runTestSubset(maxTests);
            } else {
                report = runner.runAllTests();
            }

            // Exit with error code if there are many failures
            double successRate = report.getSuccessRate();
            if (successRate < 50.0) {
                System.exit(1);
            }

        } catch (Exception e) {
            System.err.println("Error running jq4java tests: " + e.getMessage());
            if (args.length > 0 && (args[0].equals("--verbose") || args[0].equals("-v"))) {
                e.printStackTrace();
            }
            System.exit(1);
        }
    }

    private static void printUsage() {
        System.out.println("jq4java Test Runner");
        System.out.println();
        System.out.println("Usage: java CompatibilityTestRunner [options]");
        System.out.println();
        System.out.println("Options:");
        System.out.println("  --verbose, -v              Enable verbose output");
        System.out.println("  --max-tests <n>            Run only first n tests");
        System.out.println("  --filter <pattern>         Run only tests containing pattern in program");
        System.out.println("  --jq-reference-dir <dir>   Path to jq source code directory (required)");
        System.out.println("  --help, -h                 Show this help message");
    }
}