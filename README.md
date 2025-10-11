# jq4java

[![Build Status](https://github.com/dortegau/jq4java/actions/workflows/test.yml/badge.svg)](https://github.com/dortegau/jq4java/actions)
[![Java Version](https://img.shields.io/badge/Java-8%2B-blue)](https://www.oracle.com/java/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

A Java 8 port of [jq](https://jqlang.github.io/jq/), the command-line JSON processor.

## Features

- ✅ **Java 8 compatible** - Works with Java 8+
- ✅ **ANTLR-based parser** - Formal grammar for robust parsing
- ✅ **CI/CD** - Automated testing on GitHub Actions

## Implemented

- Literals: `true`, `false`, `null`, numbers
- Identity: `.`
- Field access: `.foo`, `.foo.bar`
- Pipe: `|`
- Array iteration: `.[]`
- Array indexing: `.[0]`, `.[-1]`
- Array slicing: `.[1:3]`, `.[:2]`, `.[2:]`
- Array construction: `[.a, .b]`
- Object construction: `{a: .x, b: .y}`
- Comma operator: `.a, .b` (multiple outputs)

## Usage

```java
import com.dortegau.jq4java.Jq;

String result = Jq.execute(".foo", "{\"foo\": 42}");
// result: "42"

String result = Jq.execute("[.a, .b]", "{\"a\":1, \"b\":2}");
// result: "[1,2]"

String result = Jq.execute(".users[0].email", "{\"users\":[{\"email\":\"test@example.com\"}]}");
// result: "\"test@example.com\""
```

## Development

Requires Maven 3+ and Java 8+. ANTLR4 grammar is automatically compiled during build.

```bash
# Generate parser and run tests
mvn clean test

# Generate parser only
mvn generate-sources
```

## Architecture

```
com.dortegau.jq4java/
├── ast/              # AST nodes (Expression implementations)
├── parser/           # ANTLR grammar and AST builder
├── json/             # JSON abstraction (JqValue interface + implementations)
└── Jq.java           # Public API
```

The parser uses ANTLR4 to generate a formal grammar parser, then `JqAstBuilder` converts the parse tree into AST nodes. All JSON library usage is isolated in the `json` package, making it easy to swap implementations.

## Status

This is a work in progress.

## License

MIT
