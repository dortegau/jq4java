# jq4java-cli

Command-line interface for [jq4java](../jq4java-core).

## Build

```bash
# From root directory
mvn clean package

# Or just CLI
cd jq4java-cli
mvn clean package
```

This creates `jq4java-cli/target/jq4java.jar` - a standalone executable JAR.

## Usage

```bash
# Run directly
java -jar jq4java-cli/target/jq4java.jar '.' input.json

# Or create an alias
alias jq4java='java -jar /path/to/jq4java.jar'

# Then use like jq
jq4java '.' input.json
jq4java '.foo' input.json
echo '{"a":1}' | jq4java '.a'
```

## Options

```
jq4java [OPTIONS] <filter> [file]

Options:
  -h, --help              Show help
  -V, --version           Show version
```

## Examples

```bash
# Identity
jq4java '.' input.json

# Field access
jq4java '.users[0].name' data.json

# Pipe from stdin
echo '{"name":"Alice","age":30}' | jq4java '.name'

# Comparison operators
jq4java '[.age > 18, .price <= 100]' data.json
```
