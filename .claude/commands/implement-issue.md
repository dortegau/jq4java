# Implement Issue Command

Implement a specific GitHub issue following TDD workflow, architecture rules, and project conventions, then create a Pull Request automatically.

## Purpose

This command runs only the **implementation phase** of the development workflow. Use this when you want to:

- Implement a specific existing GitHub issue
- Resume interrupted implementation work
- Implement issues created manually or by other processes
- Skip the analysis phase when you know exactly what to build

## Arguments

- `<issue_number>` (optional): Specific issue number to implement
- If not provided, automatically finds the most recent high-priority issue

## Workflow

1. **Setup**: Launch the `github-task-executor` agent to:
   - Find and claim the target issue (latest high-priority or specified number)
   - Create appropriate feature branch
   - Validate issue requirements

2. **Implementation**: Execute complete TDD workflow:
   - Validate expected behavior with native jq CLI
   - Write comprehensive failing tests first
   - Implement minimum code to pass tests
   - Add error handling and edge case tests
   - Ensure architecture compliance

3. **Finalization**:
   - Update documentation and README
   - Create commit with proper messaging
   - Push branch and create Pull Request automatically
   - Report final state for next phase

## Usage

```
/implement-issue              # Implement latest high-priority issue
/implement-issue 123          # Implement specific issue #123
```

## Output

The command will implement the feature completely and create a Pull Request ready for review.

---

**Arguments**: {args}

Launch github-task-executor agent to implement the specified issue (or latest high-priority issue if none specified) following TDD workflow, architecture rules, and project conventions. The agent will create the Pull Request automatically upon completion.