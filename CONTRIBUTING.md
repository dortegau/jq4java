# Contributing to jq4java

Thank you for your interest in contributing to jq4java!

## Development Workflow

### 1. Create an Issue First

Before starting work on a new feature or bug fix:
- Create a GitHub Issue describing what you want to implement
- Discuss the approach if it's a significant change
- Reference the issue number in your commits and PR

### 2. Work in a Branch

The `main` branch is protected. All changes must go through Pull Requests.

```bash
# Create a new branch from main
git checkout main
git pull
git checkout -b feature/your-feature-name

# Or for bug fixes
git checkout -b fix/issue-number-description
```

### 3. Follow TDD (Test-Driven Development)

**Always write tests before implementing features:**

1. Add failing tests in `JqTest` (happy path) or `JqErrorTest` (error cases)
2. Run tests to verify they fail: `./mvnw test`
3. Implement the feature to make tests pass
4. Add combined tests mixing the new feature with existing ones
5. Update documentation (README.md, IMPLEMENTATION_STATUS.md if exists)

### 4. Test Organization

- `JqTest` - ALL integration/happy path tests using `Jq.execute()` API
- `JqErrorTest` - ALL error/failure tests using `Jq.execute()` API
- NO separate test classes for individual features
- Group tests by feature using method names (e.g., `testFieldAccess`, `testAlternativeOperator`)

### 5. Commit and Push

```bash
# Make your changes
git add .
git commit -m "Add feature X (#issue-number)"

# Push your branch
git push origin feature/your-feature-name
```

### 6. Create a Pull Request

- Go to GitHub and create a PR from your branch to `main`
- Reference the issue number in the PR description
- Wait for CI checks to pass
- Merge the PR (no approval required for now)

## Architecture Rules

### Core Principles

1. **No external dependencies in AST layer**
   - Classes in `com.dortegau.jq4java.ast` MUST NOT import any external libraries
   - AST nodes should only depend on `JqValue` interface, never concrete implementations

2. **JSON library isolation**
   - ALL usage of external JSON libraries (org.json, Gson, Jackson, etc.) MUST be contained in `com.dortegau.jq4java.json` package
   - Only `JqValue` implementations can use external JSON libraries
   - The rest of the codebase interacts only through `JqValue` interface

3. **Dependency direction**
   ```
   AST → JqValue (interface) ← JsonAdapter (implementation)
   ```

### Built-in Functions

Every built-in function MUST register itself in `BuiltinRegistry`:

```java
static {
    BuiltinRegistry.register("functionName", arity);
}
```

## Code Style

- **Language**: ALL code, comments, and documentation MUST be in English
- **Comments**: Be surgical - only add when absolutely necessary
- **Self-documenting code**: Prefer clear code over comments
- Comments should explain WHY, not WHAT

### Checkstyle Compliance (REQUIRED)

**All code MUST pass checkstyle validation before merging:**

```bash
# Validate your code against checkstyle rules
./mvnw checkstyle:check

# This must show: "You have 0 Checkstyle violations"
```

**Key requirements:**
- **Javadoc**: All public classes and methods must have comprehensive documentation
- **Line length**: Maximum 100 characters per line
- **Import order**: Lexicographic ordering with proper grouping
- **Method organization**: Group overloaded methods together
- **Formatting**: Consistent spacing and structure

**CI Integration**: Builds will automatically fail if checkstyle violations are detected. This ensures consistent code quality across all contributions.

**Pre-commit validation** (optional but recommended):
```bash
# Run checkstyle before every commit
cp scripts/pre-commit-checkstyle.sh .git/hooks/pre-commit
chmod +x .git/hooks/pre-commit
```

## Running Tests

```bash
# Run unit tests
./mvnw test

# Run jq test suite (first clone jq source code, then run tests)
git clone https://github.com/jqlang/jq.git
java -jar jq4java-cli/target/jq4java.jar --run-tests jq/tests/jq.test
```

## Questions?

Open an issue for questions or discussions about contributing.
