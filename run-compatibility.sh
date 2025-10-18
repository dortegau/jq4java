#!/bin/bash

# jq4java Compatibility Test Runner
# Usage: ./run-compatibility.sh [options]

# Store the original directory
SCRIPT_DIR="$(dirname "$0")"
ORIGINAL_DIR="$(pwd)"

# Default values
MAX_TESTS=""
VERBOSE=""
FILTER=""
JQ_SOURCE_CODE_DIR=""

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --max-tests)
            MAX_TESTS="--max-tests $2"
            shift 2
            ;;
        --verbose|-v)
            VERBOSE="--verbose"
            shift
            ;;
        --filter)
            FILTER="--filter $2"
            shift 2
            ;;
        --jq-source-code-dir)
            JQ_SOURCE_CODE_DIR="$2"
            shift 2
            ;;
        --help|-h)
            echo "jq4java Compatibility Test Runner"
            echo
            echo "Usage: $0 [options]"
            echo
            echo "Options:"
            echo "  --jq-source-code-dir <dir>   Path to jq source code directory (required)"
            echo "  --max-tests <n>              Run only first n tests"
            echo "  --verbose, -v                Enable verbose output"
            echo "  --filter <pattern>           Run only tests containing pattern"
            echo "  --help, -h                   Show this help"
            echo
            echo "Examples:"
            echo "  $0 --jq-source-code-dir ../jq_source_code --max-tests 20    # Run first 20 tests"
            echo "  $0 --jq-source-code-dir ../jq_source_code --filter 'map'    # Run tests containing 'map'"
            echo "  $0 --jq-source-code-dir ../jq_source_code --verbose         # Run all tests with verbose output"
            exit 0
            ;;
        *)
            echo "Unknown option: $1"
            echo "Use --help for usage information"
            exit 1
            ;;
    esac
done

# Validate required parameters
if [ -z "$JQ_SOURCE_CODE_DIR" ]; then
    echo "Error: --jq-source-code-dir is required"
    echo "Use --help for usage information"
    exit 1
fi

# Convert JQ_SOURCE_CODE_DIR to absolute path
if [[ "$JQ_SOURCE_CODE_DIR" = /* ]]; then
    # Already absolute path
    ABS_JQ_SOURCE_CODE_DIR="$JQ_SOURCE_CODE_DIR"
else
    # Convert relative path to absolute path
    ABS_JQ_SOURCE_CODE_DIR="$ORIGINAL_DIR/$JQ_SOURCE_CODE_DIR"
fi

# Change to jq4java-core directory for running Maven
cd "$SCRIPT_DIR/jq4java-core"

# Build the arguments string
ARGS="--jq-reference-dir $ABS_JQ_SOURCE_CODE_DIR $MAX_TESTS $VERBOSE $FILTER"

echo "🚀 Running jq4java compatibility tests..."
echo

# Run the compatibility test runner
../mvnw exec:java \
    -Dexec.mainClass="com.dortegau.jq4java.compatibility.CompatibilityTestRunner" \
    -Dexec.args="$ARGS" \
    -Dexec.classpathScope=test \
    -q