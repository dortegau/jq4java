# jq4java

[![Build Status](https://github.com/dortegau/jq4java/actions/workflows/test.yml/badge.svg)](https://github.com/dortegau/jq4java/actions)
[![Java Version](https://img.shields.io/badge/Java-8%2B-blue)](https://www.oracle.com/java/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

A Java 8+ port of [jq](https://jqlang.github.io/jq/), the lightweight command-line JSON processor.

## Why jq4java?

**Use jq's powerful query language directly in your Java applications:**

- **Familiar syntax** - If you know jq, you already know how to use it
- **Embeddable** - No need to shell out to external jq binary
- **Type-safe** - Compile-time checking of your Java code (runtime checking of jq expressions)
- **Lightweight** - Minimal dependencies, works with Java 8+
- **Testable** - Easy to unit test your JSON transformations

**Perfect for:**
- Processing JSON APIs responses in Java applications
- ETL pipelines that need complex JSON transformations
- Testing tools that validate JSON structures
- Microservices that manipulate JSON configurations
- Any Java app that needs more than simple JSON parsing

**Full disclosure:** This project was primarily created as a playground for testing AI-assisted software development, particularly with Claude Code and its subagents, alongside Amazon Q Developer. While the implementation is solid and the tool actually works (surprisingly well!), the real experiment here is seeing how far we can push AI pair programming, automated testing, PR workflows, and systematic feature development. If you find the library useful for real work, that's a delightful bonus! 🤖✨

## Modules

- **[jq4java-core](jq4java-core/)** - Core library for embedding in Java applications
- **[jq4java-cli](jq4java-cli/)** - Command-line interface

## Features

- ✅ **Java 8 compatible** - Works with Java 8+
- ✅ **ANTLR-based parser** - Formal grammar for robust parsing
- ✅ **Library + CLI** - Use as library or standalone tool
- ✅ **CI/CD** - Automated testing on GitHub Actions

## Implemented

- Literals: `true`, `false`, `null`, numbers, strings (`"hello"`)
- String interpolation: `"Hello, \(.name)!"`
- Identity: `.`
- Field access: `.foo`, `.foo.bar`, `."my-key"`, `.["foo"]`
- Pipe: `|`
- Array iteration: `.[]`
- Array indexing: `.[0]`, `.[-1]`
- Array slicing: `.[1:3]`, `.[:2]`, `.[2:]`
- Array construction: `[.a, .b]`
- Object construction: `{a: .x, b: .y}`, `{"my-key": .value}`, `{a, b}` (shorthand)
- Comma operator: `.a, .b` (multiple outputs)
- Alternative operator: `.foo // "default"` (null/false coalescing)
- Comparison operators: `==`, `!=`, `<`, `<=`, `>`, `>=`
- Arithmetic operators: `+`, `-`, `*`, `/`, `%` (also string/array concatenation with `+`)
- Logical operators: `and`, `or`, `not`
- Conditional expressions: `if-then-else-end`, `if-then-elif-then-else-end`, `if-then-end` (optional else)
- Built-in functions: `length`, `utf8bytelength`, `keys`, `type`, `map(expr)`, `select(expr)`, `builtins`, `flatten`, `add`, `abs`, `sort`, `reverse`, `unique`, `transpose`, `range(n)`, `range(from; to)`, `range(from; to; step)`, `to_entries`, `from_entries`, `tojson`, `fromjson`, `with_entries(expr)`
  - `utf8bytelength`: returns the number of bytes required to encode a string in UTF-8 and errors for non-string values
- Format filters: `@text`, `@json`, `@html`, `@csv`, `@tsv`, `@sh`, `@base64`, `@base64d`, `@uri`, `@urid`

## Usage

### As a Library

```java
import com.dortegau.jq4java.Jq;

String result = Jq.execute(".foo", "{\"foo\": 42}");
// result: "42"

String result = Jq.execute("[.a, .b]", "{\"a\":1, \"b\":2}");
// result: "[1,2]"

String result = Jq.execute("{a, b}", "{\"a\":1, \"b\":2, \"c\":3}");
// result: "{\"a\":1,\"b\":2}"

String result = Jq.execute(".users[0].email", "{\"users\":[{\"email\":\"test@example.com\"}]}");
// result: "\"test@example.com\""

String result = Jq.execute("if .score >= 90 then \"A\" elif .score >= 80 then \"B\" else \"F\" end", "{\"score\": 95}");
// result: "\"A\""

String result = Jq.execute("if .active then \"enabled\" else \"disabled\" end", "{\"active\": true}");
// result: "\"enabled\""

// Interpolated strings mix plain text with filter results
String greeting = "\"Hello, \(.name)!\"";
String result = Jq.execute(greeting, "{\"name\": \"Ada\"}");
// result: "\"Hello, Ada!\""

// Format filters for base64 encoding/decoding
String result = Jq.execute("@base64", "\"hello\"");
// result: "\"aGVsbG8=\""

String result = Jq.execute("@base64d", "\"aGVsbG8=\"");
// result: "\"hello\""

// Format filters for percent-encoding strings
String result = Jq.execute("@uri", "\"μ\"");
// result: "\"%CE%BC\""

String result = Jq.execute("@urid", "\"%CE%BC\"");
// result: "\"μ\""

// Additional format filters for structured text output
String result = Jq.execute("@text", "{\"greeting\":\"hello\"}");
// result: "\"{\"\\\"greeting\\\":\"hello\"}\""

String result = Jq.execute("@csv", "[\"a\",\"b, c\"]");
// result: "\"a,\"\"b, c\"\"\""

String result = Jq.execute("@sh", "[\"foo\",\"bar baz\"]");
// result: "\"'foo' 'bar baz'\""

// Note: Like jq, the bare names `base64`/`base64d` and `uri`/`urid` are not valid functions.

// Array manipulation functions
String result = Jq.execute("flatten", "[[1,2],[3,4]]");
// result: "[1,2,3,4]"

String result = Jq.execute("add", "[1,2,3,4]");
// result: "10"

String result = Jq.execute("sort", "[3,1,4,2]");
// result: "[1,2,3,4]"

String result = Jq.execute("reverse", "[1,2,3]");
// result: "[3,2,1]"

String result = Jq.execute("unique", "[1,2,2,3,1]");
// result: "[1,2,3]"

String result = Jq.execute("transpose", "[[1,2],[3,4]]");
// result: "[[1,3],[2,4]]"

// Range functions for sequence generation
String result = Jq.execute("range(5)", "null");
// result: "0\n1\n2\n3\n4"

String result = Jq.execute("range(2; 7)", "null");
// result: "2\n3\n4\n5\n6"

String result = Jq.execute("range(0; 10; 2)", "null");
// result: "0\n2\n4\n6\n8"

// Object-array transformation functions
String result = Jq.execute("to_entries", "{\"a\": 1, \"b\": 2}");
// result: "[{\"key\":\"a\",\"value\":1},{\"key\":\"b\",\"value\":2}]"

String result = Jq.execute("to_entries", "[\"hello\", \"world\"]");
// result: "[{\"key\":0,\"value\":\"hello\"},{\"key\":1,\"value\":\"world\"}]"

String result = Jq.execute("from_entries", "[{\"key\": \"a\", \"value\": 1}, {\"key\": \"b\", \"value\": 2}]");
// result: "{\"a\":1,\"b\":2}"

String result = Jq.execute("to_entries | from_entries", "{\"x\": 42, \"y\": 99}");
// result: "{\"x\":42,\"y\":99}"

// Advanced object transformations with with_entries
String result = Jq.execute("with_entries({key: .key, value: (.value + 1)})", "{\"a\": 1, \"b\": 2}");
// result: "{\"a\":2,\"b\":3}"

String result = Jq.execute("with_entries(select(.key == \"b\"))", "{\"a\": 1, \"b\": 2, \"c\": 3}");
// result: "{\"b\":2}"

String result = Jq.execute("with_entries({key: (\"prefix_\" + .key), value: .value})", "{\"name\": \"Alice\", \"age\": 30}");
// result: "{\"prefix_name\":\"Alice\",\"prefix_age\":30}"

// JSON serialization helpers
String result = Jq.execute("tojson", "{\"hello\": \"world\"}");
// result: "\"{\\\"hello\\\":\\\"world\\\"}\""

String result = Jq.execute("fromjson", "\"{\\\"hello\\\":\\\"world\\\"}\"");
// result: "{\"hello\":\"world\"}"
```

#### Reuse precompiled jq expressions

When you need to evaluate the same jq program repeatedly, compile it once and keep the
resulting `Expression`. Doing so prevents rebuilding the AST on every execution.

```java
import com.dortegau.jq4java.Jq;
import com.dortegau.jq4java.ast.Expression;
import com.dortegau.jq4java.json.JqValue;
import com.dortegau.jq4java.json.OrgJsonValue;

Expression projection = Jq.compile(".user.email");

String first = Jq.execute(projection, "{\"user\":{\"email\":\"a@example.com\"}}");
String second = Jq.execute(projection, "{\"user\":{\"email\":\"b@example.com\"}}");

// You can also reuse parsed JSON inputs
JqValue input = OrgJsonValue.parse("{\"user\":{\"email\":\"c@example.com\"}}");
String third = Jq.execute(projection, input);
```

This pattern is particularly helpful in pipelines that apply the same jq query to many
documents—such as API integrations, batch processing, or repeated validations.

### As a CLI

```bash
# Build
$ ./mvnw clean package

# Run
$ java -jar jq4java-cli/target/jq4java.jar '.' input.json
$ echo '{"a":1}' | java -jar jq4java-cli/target/jq4java.jar '.a'

# Or use the wrapper script
$ ./jq4java-cli/jq4java '.name' data.json
```

See [jq4java-cli/README.md](jq4java-cli/README.md) for more CLI options.

## Development

Requires Java 8+. No need to install Maven - the project includes Maven Wrapper. ANTLR4 grammar is automatically compiled during build.

```bash
# Build everything
$ ./mvnw clean package

# Run tests
$ ./mvnw test

# Run checkstyle validation (REQUIRED before commits)
$ ./mvnw checkstyle:check

# Run jq test suite (first clone jq source code, then run tests)
$ git clone https://github.com/jqlang/jq.git
$ java -jar jq4java-cli/target/jq4java.jar --run-tests jq/tests/jq.test

# Install git hooks (auto-updates README stats on push)
$ bash scripts/install-hooks.sh

# Install checkstyle pre-commit hook (optional but recommended)
$ cp scripts/pre-commit-checkstyle.sh .git/hooks/pre-commit
$ chmod +x .git/hooks/pre-commit

# Build only core library
$ cd jq4java-core && ../mvnw clean package

# Build only CLI
$ cd jq4java-cli && ../mvnw clean package
```

### Code Quality

This project enforces strict **checkstyle compliance**:

- ✅ **All public classes and methods** must have comprehensive Javadoc documentation
- ✅ **Line length limit**: 100 characters maximum
- ✅ **Import organization**: Lexicographic ordering with proper grouping
- ✅ **Method organization**: Overloaded methods must be grouped together
- ✅ **Consistent formatting**: Proper spacing and code structure

**CI Integration**: All builds automatically fail if checkstyle violations are detected, ensuring consistent code quality across all contributions.

## GitHub Actions workflows

| Workflow | File | Purpose |
| --- | --- | --- |
| Tests | `.github/workflows/test.yml` | Runs the Maven build and test suite on every push and pull request to ensure changes remain green. |
| Deploy Javadoc | `.github/workflows/javadoc-pages.yml` | Generates the aggregated project Javadoc with Maven and publishes the rendered documentation to GitHub Pages. |

## Architecture

```
jq4java/
├── jq4java-core/                    # Core library
│   ├── src/main/antlr4/             # ANTLR grammar source
│   │   └── com/dortegau/jq4java/parser/
│   │       └── JqGrammar.g4         # jq grammar definition
│   ├── target/generated-sources/antlr4/ # Generated ANTLR files (build-time)
│   │   └── com/dortegau/jq4java/parser/
│   │       ├── JqGrammarLexer.java  # Generated lexer
│   │       └── JqGrammarParser.java # Generated parser
│   └── src/main/java/com/dortegau/jq4java/
│       ├── ast/                     # AST nodes (Expression implementations)
│       ├── parser/                  # Parser integration and AST builder
│       │   ├── JqParser.java        # Parser wrapper
│       │   └── JqAstBuilder.java    # Converts parse tree to AST
│       ├── json/                    # JSON abstraction (JqValue interface + implementations)
│       └── Jq.java                  # Public API
└── jq4java-cli/                     # Command-line interface
    └── src/main/java/com/dortegau/jq4java/cli/
        └── JqCli.java               # CLI entry point
```

The parser uses ANTLR4 to generate a formal grammar parser, then `JqAstBuilder` converts the parse tree into AST nodes. All JSON library usage is isolated in the `json` package, making it easy to swap implementations.

The CLI is a separate module that depends on the core library, keeping the library lightweight for embedded use.

## Status

This is a work in progress. Known bugs and issues are tracked in [GitHub Issues](https://github.com/dortegau/jq4java/issues).

## License

MIT

