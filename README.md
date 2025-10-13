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

**jq compatibility: 49/595 official jq tests passing (8%)**
- Literals: `true`, `false`, `null`, numbers, strings (`"hello"`)
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
- Built-in functions: `length`, `keys`, `type`, `map(expr)`, `select(expr)`, `builtins`

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
```

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

# Run jq-reference test suite
$ java -jar jq4java-cli/target/jq4java.jar --run-tests jq-reference/tests/jq.test

# Install git hooks (auto-updates README stats on push)
$ bash scripts/install-hooks.sh

# Build only core library
$ cd jq4java-core && ../mvnw clean package

# Build only CLI
$ cd jq4java-cli && ../mvnw clean package
```

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

