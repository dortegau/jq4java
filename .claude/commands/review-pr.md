# Review PR Command

Perform a comprehensive review of a GitHub Pull Request for code quality, security, documentation updates, test coverage, and adherence to project standards.

## Purpose

This command runs only the **review phase** of the development workflow. Use this when you want to:

- Review any Pull Request (not just auto-generated ones)
- Re-review a PR after changes have been made
- Review external contributions from other developers
- Get detailed feedback on code quality and compliance

## Arguments

- `<pr_number>` (optional): Specific PR number to review
- If not provided, automatically finds the most recent PR from current user

## Workflow

1. **Setup**: Launch the `github-pr-reviewer` agent to:
   - Find and access the target PR (latest from current user or specified number)
   - Fetch PR details and changed files
   - Validate PR accessibility

2. **Comprehensive Analysis**:
   - **Security**: Scan for vulnerabilities, unsafe practices, input validation
   - **Documentation**: Verify README updates, code comments, architectural docs
   - **Test Coverage**: Ensure comprehensive tests in JqTest and JqErrorTest
   - **Architecture**: Check compliance with jq4java design principles
   - **Code Quality**: Review clean code practices, error handling, performance

3. **Report Generation**:
   - Provide detailed feedback with file locations and line numbers
   - Suggest specific solutions for identified issues
   - Prioritize issues (Critical, High, Medium, Low)
   - Give final recommendation (Approve, Request Changes, Needs Discussion)

## Usage

```
/review-pr              # Review latest PR from current user
/review-pr 45           # Review specific PR #45
```

## Output

The command will provide a comprehensive review report with actionable feedback and a clear recommendation.

---

**Arguments**: {args}

Launch github-pr-reviewer agent to perform comprehensive review of the specified PR (or latest PR from current user if none specified) for code quality, security, documentation updates, test coverage, and adherence to project standards.