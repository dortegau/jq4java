# Analyze Tests Command

Analyze the official jq test suite to identify and prioritize the next most important task for jq4java implementation.

## Purpose

This command runs only the **analysis and prioritization phase** of the development workflow. Use this when you want to:

- Identify the next high-priority feature to implement
- Research current test failures without implementing
- Create prioritized GitHub issues for manual implementation later
- Update existing issue priorities based on latest test results

## Workflow

1. **Test Suite Analysis**: Launch the `jq-test-prioritizer` agent to:
   - Execute jq-reference tests with jq4java-cli
   - Categorize failing tests by frequency and impact
   - Assess implementation effort and dependencies
   - Select highest impact-to-effort ratio task

2. **Issue Creation**: Create a well-structured GitHub issue with:
   - Clear feature description and failing tests
   - Implementation hints and acceptance criteria
   - Priority justification and impact estimation

## Usage

```
/analyze-tests
```

## Output

The command will create a new GitHub issue labeled with appropriate priority and provide a summary of the analysis.

---

Launch jq-test-prioritizer agent to analyze failing tests and create a prioritized GitHub issue for the most impactful next task.