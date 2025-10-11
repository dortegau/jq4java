package com.dortegau.jq4java.cli;

import com.dortegau.jq4java.Jq;

import java.io.BufferedReader;
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

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("-h") || arg.equals("--help")) {
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
            String input = readInput(inputFile);
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
        System.out.println("  -h, --help               Show this help");
        System.out.println("  -V, --version            Show version");
        System.out.println();
        System.out.println(
            "See https://github.com/dortegau/jq4java for implementation status.");
    }
}
