# Contributing to jq4java

Thanks for your interest in contributing! This project follows a simple workflow.

## Development Setup

1. Fork the repository
2. Clone your fork: `git clone https://github.com/YOUR_USERNAME/jq4java.git`
3. Ensure you have Java 8+ and Maven installed
4. Run tests: `mvn test`

## Code Style

- We use **Google Java Style Guide** enforced by Checkstyle
- Run `mvn checkstyle:check` before committing
- All code, comments, and documentation must be in **English**

## Development Process

1. **Test-Driven Development (TDD)**:
   - Write failing tests first
   - Implement the minimal code to make tests pass
   - Refactor if needed

2. **Architecture**:
   - Keep JSON library usage isolated in `com.dortegau.jq4java.json` package
   - AST nodes should only depend on `JqValue` interface
   - No external dependencies in AST layer

3. **Documentation**:
   - Update `README.md` when adding new features
   - Keep examples simple and working

## Submitting Changes

1. Create a feature branch: `git checkout -b feature/your-feature`
2. Make your changes following TDD
3. Ensure all tests pass: `mvn test`
4. Ensure code style compliance: `mvn checkstyle:check`
5. Update documentation if needed
6. Commit with clear messages
7. Push and create a Pull Request

## What to Contribute

Check the README "Implemented" section to see what's missing. Good first contributions:
- Basic jq functions (`length`, `keys`, `type`)
- More test cases for existing features
- Documentation improvements

## Questions?

Open an issue for discussion before starting major features.