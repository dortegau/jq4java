# Next Issue Command

This command orchestrates the complete workflow for identifying and implementing the next most important jq4java feature based on failing tests.

## Workflow:

1. **Analyze and Prioritize**: Use the `jq-test-prioritizer` agent to analyze the current state of failing tests from the official jq test suite and create a prioritized GitHub issue for the most impactful next task.

2. **Implement Solution**: Use the `github-task-executor` agent to automatically implement the created issue following TDD workflow, architecture rules, and project conventions. This includes creating the Pull Request automatically.

3. **Review Pull Request**: Use the `github-pr-reviewer` agent to perform a comprehensive review of the created PR for code quality, security, documentation updates, test coverage, and adherence to project standards.

## Usage:
```
/next-issue
```

This will:
- Run the jq-reference test suite to identify failing tests
- Create a prioritized GitHub issue for the most important missing feature
- Automatically implement the feature with comprehensive test coverage and create a Pull Request
- Perform automated code review of the PR
- Update documentation and README statistics
- Link PR to close the original issue

Perfect for systematic improvement of jq4java compatibility!

---

**IMPORTANT: Execute these agents SEQUENTIALLY with state validation. Each agent must complete successfully before the next one begins.**

## Step-by-Step Execution with Error Handling:

### Step 1: Analysis & Prioritization
Launch jq-test-prioritizer agent to analyze failing tests and create a prioritized GitHub issue.

**Success Criteria**: Agent reports "✅ PRIORITIZER COMPLETED SUCCESSFULLY" with issue number and URL.
**On Failure**: Stop pipeline and report error. Do not proceed to Step 2.

### Step 2: Implementation & PR Creation
**ONLY IF Step 1 succeeded**: Launch github-task-executor agent to implement the created issue following TDD workflow, architecture rules, and project conventions. This agent will also create the Pull Request automatically.

**State Validation**: Agent will automatically find the most recent high-priority issue (created in Step 1).
**Success Criteria**: Agent reports "✅ EXECUTOR COMPLETED SUCCESSFULLY" with PR number and URL.
**On Failure**: Stop pipeline, report error and cleanup recommendations.

### Step 3: Code Review
**ONLY IF Step 2 succeeded**: Launch github-pr-reviewer agent to perform comprehensive review of the created PR.

**State Validation**: Agent will automatically find the most recent PR from current user (created in Step 2).
**Success Criteria**: Agent provides review with clear recommendation (Approve/Request Changes/Needs Discussion).
**On Failure**: Report error, but PR still exists for manual review.

## Pipeline State Management:

Each agent uses **GitHub as the shared state store**:
- **Issues** carry context between prioritizer → executor
- **Pull Requests** carry context between executor → reviewer
- **Git branches** provide implementation history
- **Labels and assignments** track workflow progress

**Early Exit Strategy**: If any step fails, the pipeline stops immediately with clear error reporting and cleanup instructions.

---

Execute the complete workflow for identifying and implementing the next most important jq4java feature:

1. Launch jq-test-prioritizer agent to analyze failing tests and create a prioritized GitHub issue
2. Launch github-task-executor agent to implement the created issue and create a Pull Request
3. Launch github-pr-reviewer agent to review the created PR

Execute these agents SEQUENTIALLY with state validation - each must complete successfully before the next begins.