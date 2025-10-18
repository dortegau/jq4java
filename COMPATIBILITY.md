# jq4java Compatibility Runner

A convenient script to run jq4java against the official jq reference test suite to identify missing features and compatibility issues.

## 🚀 Quick Start

First, clone the official jq source code:
```bash
git clone https://github.com/jqlang/jq.git
```

Then run tests:
```bash
# Run all tests
./run-compatibility.sh --jq-source-code-dir ./jq

# Run only the first 20 tests
./run-compatibility.sh --jq-source-code-dir ./jq --max-tests 20

# Run tests containing 'map' in the program
./run-compatibility.sh --jq-source-code-dir ./jq --filter map

# Run with detailed output
./run-compatibility.sh --jq-source-code-dir ./jq --verbose

# Show help
./run-compatibility.sh --help
```

## 📊 Understanding Results

### jq4java Test Statistics
- **Total tests**: Total number of tests executed
- **Passed**: Tests that produced the expected result
- **Failed**: Tests that failed or produced incorrect output
- **Success rate**: Success percentage

### Failure Analysis
The runner provides detailed analysis of failing tests:

1. **Error categorization**: Groups failures by error type and frequency
2. **Detailed failure reports**: Shows program, input, expected vs actual output
3. **Missing feature identification**: Highlights unimplemented jq functionality

## 🎯 Identified Missing Features

Based on executed tests, the following features need implementation in jq4java:

### ⚠️ **Critical** (affect many tests)
- **String formatting**: `@base64`, `@html`, `@uri`, `@csv`, etc.
- **String interpolation**: `"text \(expression) more"`
- **Try/catch**: `try expression catch handler`
- **Optional operators**: `.foo?`, `[]?`

### 🔧 **Important** (advanced syntax)
- **Dynamic objects**: `{(expression): value}`
- **Assignment operations**: `(.path = value)`, `(.path |= expression)`
- **Array negative indexing**: `.foo[-1]`
- **Advanced string escapes**: `\v`, etc.

### 📈 **Improvements**
- **Error message format**: Improve error message compatibility
- **Unicode handling**: Byte order mark and special characters

## 🏗️ Runner Architecture

### Main Components

```
CompatibilityTestRunner
├── JqReferenceTestParser     # Parses .test files from jq source
├── Jq4JavaExecutor          # Executes tests using jq4java-core
└── Jq4JavaReporter          # Generates detailed jq4java-focused reports
```

### Data Models
- **JqTestCase**: Represents an individual test (program, input, expected output)
- **TestResult**: Result of executing a test (status, output, time, errors)
- **Jq4JavaReport**: Focused report with jq4java statistics and failure analysis

## 📁 File Locations

```
jq4java/
├── run-compatibility.sh                    # Convenience script
└── jq4java-core/src/test/java/com/dortegau/jq4java/compatibility/
    ├── CompatibilityTestRunner.java       # Main runner
    ├── JqReferenceTestParser.java         # Parser for .test files
    ├── Jq4JavaExecutor.java              # Executor for jq4java
    ├── Jq4JavaReporter.java               # jq4java-focused reporter
    ├── JqTestCase.java                    # Test model
    ├── TestResult.java                    # Result model
    └── Jq4JavaReport.java                 # Report model
```

You'll need to provide your own jq source directory when running the script.

## 🔍 Advanced Usage Examples

### Direct Execution with Maven
```bash
cd jq4java-core
../mvnw exec:java \
  -Dexec.mainClass="com.dortegau.jq4java.compatibility.CompatibilityTestRunner" \
  -Dexec.args="--max-tests 100 --verbose --jq-reference-dir /path/to/jq/source" \
  -Dexec.classpathScope=test
```

### Useful Filters
```bash
# Arithmetic tests
./run-compatibility.sh --jq-source-code-dir ./jq --filter "+"

# Array tests
./run-compatibility.sh --jq-source-code-dir ./jq --filter "[]"

# Object tests
./run-compatibility.sh --jq-source-code-dir ./jq --filter "{"

# Specific tests
./run-compatibility.sh --jq-source-code-dir ./jq --filter "map"
```

## 🎨 Report Features

- **Colors**: Green for success, red for failures, yellow for warnings
- **Failure details**: Shows program, input, expected vs actual output
- **Error categorization**: Groups failures by error type and frequency
- **Statistics**: Success rate, totals by category
- **Detailed analysis**: Provides actionable insights for missing features

## 🎯 Benefits

1. **Identifies precise gaps**: Shows exactly which features are missing in jq4java
2. **Guides development**: Prioritizes work based on failing tests
3. **Continuous validation**: Verifies new features don't break existing ones
4. **Focused analysis**: Concentrates on jq4java improvements with streamlined testing
5. **TDD development**: Enables implementing features following existing reference tests

This runner is a fundamental tool for making jq4java 100% compatible with jq.