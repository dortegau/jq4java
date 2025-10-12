# jq4java

[![Build Status](https://github.com/dortegau/jq4java/actions/workflows/test.yml/badge.svg)](https://github.com/dortegau/jq4java/actions)
[![Java Version](https://img.shields.io/badge/Java-8%2B-blue)](https://www.oracle.com/java/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

A Java 8 port of [jq](https://jqlang.github.io/jq/), the command-line JSON processor.

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

## Modules

- **[jq4java-core](jq4java-core/)** - Core library for embedding in Java applications
- **[jq4java-cli](jq4java-cli/)** - Command-line interface

## Features

- ✅ **Java 8 compatible** - Works with Java 8+
- ✅ **ANTLR-based parser** - Formal grammar for robust parsing
- ✅ **Library + CLI** - Use as library or standalone tool
- ✅ **CI/CD** - Automated testing on GitHub Actions

## Implemented

**jq compatibility: 38/595 official jq tests passing (6%)**
- Literals: `true`, `false`, `null`, numbers, strings (`"hello"`)
- Identity: `.`
- Field access: `.foo`, `.foo.bar`, `."my-key"`, `.["foo"]`
- Pipe: `|`
- Array iteration: `.[]`
- Array indexing: `.[0]`, `.[-1]`
- Array slicing: `.[1:3]`, `.[:2]`, `.[2:]`
- Array construction: `[.a, .b]`
- Object construction: `{a: .x, b: .y}`, `{"my-key": .value}`
- Comma operator: `.a, .b` (multiple outputs)
- Alternative operator: `.foo // "default"` (null/false coalescing)
- Comparison operators: `==`, `!=`, `<`, `<=`, `>`, `>=`
- Arithmetic operators: `+`, `-`, `*`, `/`, `%` (also string/array concatenation with `+`)
- Logical operators: `and`, `or`, `not`
- Built-in functions: `length`, `keys`, `type`, `map(expr)`, `builtins`

## Usage

### As a Library

```java
import com.dortegau.jq4java.Jq;

String result = Jq.execute(".foo", "{\"foo\": 42}");
// result: "42"

String result = Jq.execute("[.a, .b]", "{\"a\":1, \"b\":2}");
// result: "[1,2]"

String result = Jq.execute(".users[0].email", "{\"users\":[{\"email\":\"test@example.com\"}]}");
// result: "\"test@example.com\""
```

### As a CLI

```bash
# Build
mvn clean package

# Run
java -jar jq4java-cli/target/jq4java.jar '.' input.json
echo '{"a":1}' | java -jar jq4java-cli/target/jq4java.jar '.a'

# Or use the wrapper script
./jq4java-cli/jq4java '.name' data.json
```

See [jq4java-cli/README.md](jq4java-cli/README.md) for more CLI options.

## Development

Requires Maven 3+ and Java 8+. ANTLR4 grammar is automatically compiled during build.

```bash
# Build everything
mvn clean package

# Run tests
mvn test

# Run jq-reference test suite
java -jar jq4java-cli/target/jq4java.jar --run-tests jq-reference/tests/jq.test

# Install git hooks (auto-updates README stats on push)
bash scripts/install-hooks.sh

# Build only core library
cd jq4java-core && mvn clean package

# Build only CLI
cd jq4java-cli && mvn clean package
```

## Architecture

```
jq4java/
├── jq4java-core/                    # Core library
│   └── com.dortegau.jq4java/
│       ├── ast/                     # AST nodes (Expression implementations)
│       ├── parser/                  # ANTLR grammar and AST builder
│       ├── json/                    # JSON abstraction (JqValue interface + implementations)
│       └── Jq.java                  # Public API
└── jq4java-cli/                     # Command-line interface
    └── com.dortegau.jq4java.cli/
        └── JqCli.java               # CLI entry point
```

The parser uses ANTLR4 to generate a formal grammar parser, then `JqAstBuilder` converts the parse tree into AST nodes. All JSON library usage is isolated in the `json` package, making it easy to swap implementations.

The CLI is a separate module that depends on the core library, keeping the library lightweight for embedded use.

## Status

This is a work in progress. Known bugs and issues are tracked in [GitHub Issues](https://github.com/dortegau/jq4java/issues).

## License

MIT

