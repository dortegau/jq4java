# jq4java

[![Build Status](https://github.com/dortegau/jq4java/actions/workflows/test.yml/badge.svg)](https://github.com/dortegau/jq4java/actions)
[![Java Version](https://img.shields.io/badge/Java-8%2B-blue)](https://www.oracle.com/java/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

A Java 8 port of [jq](https://jqlang.github.io/jq/), the lightweight and flexible command-line JSON processor.

## Features

- ✅ **Java 8 compatible** - Works with Java 8+
- ✅ **Zero dependencies** (except org.json for JSON parsing)
- ✅ **Clean architecture** - JSON library isolated and replaceable
- ✅ **TDD approach**
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
```

## Build

```bash
mvn clean test
```

## Status

This is a work in progress.

## License

MIT
