#!/bin/bash

# Pre-commit hook for checkstyle validation
# This script runs checkstyle and prevents commits if violations are found

echo "Running checkstyle validation..."

# Run checkstyle check
./mvnw checkstyle:check

# Check the exit status
if [ $? -ne 0 ]; then
    echo ""
    echo "❌ COMMIT REJECTED: Checkstyle violations found!"
    echo "Please fix the violations shown above and try again."
    echo ""
    echo "To fix violations:"
    echo "  1. Review the checkstyle output above"
    echo "  2. Fix the issues in your code"
    echo "  3. Run './mvnw checkstyle:check' to verify fixes"
    echo "  4. Commit again"
    echo ""
    echo "To temporarily skip this check (NOT recommended):"
    echo "  git commit --no-verify"
    echo ""
    exit 1
fi

echo "✅ Checkstyle validation passed!"
exit 0