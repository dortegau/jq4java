# Create PR Command

Create a professional Pull Request from the current feature branch to the main branch.

## Purpose

This is a **utility command** for creating Pull Requests outside the normal workflow. Use this when you want to:

- Create a PR from manual implementations (not using github-task-executor)
- Create PR with custom formatting or special requirements
- Handle edge cases not covered by automated workflow
- Create PRs for hotfixes, documentation updates, or refactoring

## Prerequisites

- Must be on a feature branch (not main)
- Branch must have committed changes ready for review
- Changes should be tested and working

## Workflow

1. **Branch Validation**:
   - Verify current branch is not main
   - Check that branch has commits ahead of main
   - Ensure working directory is clean

2. **PR Creation**:
   - Generate professional PR title based on branch name and commits
   - Create comprehensive PR description with summary and test plan
   - Link to related issues if detected from commit messages
   - Push branch if not already pushed

3. **State Reporting**:
   - Provide PR URL and number for reference
   - Report status and next recommended actions

## Usage

```
/create-pr
```

**Note**: This command operates on the current branch and will prompt for confirmation if the setup looks unusual.

## Output

The command will create a Pull Request and provide the PR URL for review.

---

Create a Pull Request from the current feature branch to main branch with professional formatting and comprehensive description.