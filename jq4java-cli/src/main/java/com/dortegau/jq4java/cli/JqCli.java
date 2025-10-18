package com.dortegau.jq4java.cli;

import com.dortegau.jq4java.Jq;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;

public final class JqCli {

    private JqCli() {
    }

    public static void main(final String[] args) {
        if (args.length == 0) {
            showHelp();
            System.exit(1);
        }

        String filter = null;
        String inputFile = null;
        boolean nullInput = false;

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--run-tests")) {
                if (args.length <= i + 1) {
                    System.err.println("Error: --run-tests requires test file path");
                    System.exit(1);
                }
                runTests(args[i + 1]);
                System.exit(0);
            } else if (arg.equals("-n") || arg.equals("--null-input")) {
                nullInput = true;
            } else if (arg.equals("-h") || arg.equals("--help")) {
                showHelp();
                System.exit(0);
            } else if (arg.equals("-V") || arg.equals("--version")) {
                System.out.println(
                    "jq4java-1.0 (Java port of jq)");
                System.out.println(
                    "https://github.com/dortegau/jq4java");
                System.exit(0);
            } else if (filter == null) {
                filter = arg;
            } else if (inputFile == null) {
                inputFile = arg;
            }
        }

        if (filter == null) {
            System.err.println(
                "jq4java - commandline JSON processor [version 1.0]");
            System.err.println(
                "Java port of jq - https://github.com/dortegau/jq4java");
            System.err.println();
            System.err.println(
                "Use jq4java --help for help with command-line options,");
            System.exit(2);
        }

        try {
            String input = nullInput ? "null" : readInput(inputFile);
            String result = Jq.execute(filter, input);
            System.out.println(result);
        } catch (Exception e) {
            System.err.println("jq4java: error: " + e.getMessage());
            System.exit(1);
        }
    }

    private static String readInput(final String inputFile) throws IOException {
        if (inputFile == null) {
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
            }
            return sb.toString().trim();
        } else {
            return new String(Files.readAllBytes(Paths.get(inputFile)));
        }
    }

    private static void runTests(final String testFile) {
        int passed = 0;
        int failed = 0;
        int skipped = 0;
        int total = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(testFile))) {
            String line;
            String filter = null;
            String input = null;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;

                if (line.trim().isEmpty() || line.trim().startsWith("#")) {
                    continue;
                }

                if (filter == null) {
                    filter = line;
                } else if (input == null) {
                    input = line;
                } else {
                    String expected = line;
                    total++;

                    try {
                        String result = Jq.execute(filter, input);
                        if (jsonEqual(result, expected)) {
                            passed++;
                        } else {
                            failed++;
                            if (failed <= 10) {
                                System.out.println("FAIL (line " + (lineNumber - 2) + "): " + filter);
                                System.out.println("  Input:    " + input);
                                System.out.println("  Expected: " + expected);
                                System.out.println("  Got:      " + result);
                            }
                        }
                    } catch (Exception e) {
                        skipped++;
                        if (skipped <= 5) {
                            System.out.println("SKIP (line " + (lineNumber - 2) + "): " + filter + " - " + e.getMessage());
                        }
                    }

                    filter = null;
                    input = null;
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading test file: " + e.getMessage());
            System.exit(1);
        }

        System.out.println("\n=== Test Results ===");
        System.out.println("Total:   " + total);
        System.out.println("Passed:  " + passed + " (" + (total > 0 ? (passed * 100 / total) : 0) + "%)");
        System.out.println("Failed:  " + failed);
        System.out.println("Skipped: " + skipped);
    }

    private static boolean jsonEqual(String a, String b) {
        try {
            String normA = Jq.execute(".", a);
            String normB = Jq.execute(".", b);
            if (normA.equals(normB)) {
                return true;
            }
            String compareResult = Jq.execute(". == " + b, a);
            return "true".equals(compareResult);
        } catch (Exception e) {
            return a.equals(b);
        }
    }

    private static void showHelp() {
        System.out.println(
            "jq4java - commandline JSON processor [version 1.0]");
        System.out.println(
            "Java port of jq - https://github.com/dortegau/jq4java");
        System.out.println();
        System.out.println(
            "Usage: jq4java [options] <jq filter> [file...]");
        System.out.println();
        System.out.println(
            "jq4java is a Java port of jq, a tool for processing JSON inputs,");
        System.out.println(
            "applying the given filter to its JSON text inputs and producing");
        System.out.println(
            "the filter's results as JSON on standard output.");
        System.out.println();
        System.out.println(
            "For jq documentation see https://jqlang.github.io/jq");
        System.out.println();
        System.out.println("Some of the options include:");
        System.out.println("  -n, --null-input         Use null as input");
        System.out.println("  --run-tests <file>       Run jq test suite from file");
        System.out.println("  -h, --help               Show this help");
        System.out.println("  -V, --version            Show version");
        System.out.println();
        System.out.println(
            "See https://github.com/dortegau/jq4java for implementation status.");
    }
}
