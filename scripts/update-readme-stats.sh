#!/bin/bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR/.."

echo "Running jq-reference tests..."

# Build if needed
if [ ! -f "jq4java-cli/target/jq4java.jar" ]; then
    mvn clean package -DskipTests -q
fi

# Run tests and capture results
RESULTS=$(java -jar jq4java-cli/target/jq4java.jar --run-tests jq-reference/tests/jq.test 2>&1 | tail -5)

# Extract passed and total
PASSED=$(echo "$RESULTS" | grep "Passed:" | awk '{print $2}')
TOTAL=$(echo "$RESULTS" | grep "Total:" | awk '{print $2}')

if [ -z "$PASSED" ] || [ -z "$TOTAL" ]; then
    echo "Error: Could not run tests"
    exit 1
fi

PERCENT=$((PASSED * 100 / TOTAL))

echo "Tests: $PASSED/$TOTAL ($PERCENT%)"

# Update README.md - replace existing compatibility line or add after "## Implemented"
if grep -q "jq compatibility:" README.md; then
    # Update existing line
    sed -i.bak "s/\*\*jq compatibility:.*/**jq compatibility: $PASSED\/$TOTAL official jq tests passing ($PERCENT%)**/" README.md
else
    # Add new line after "## Implemented"
    sed -i.bak "/## Implemented/a\\
\\
**jq compatibility: $PASSED/$TOTAL official jq tests passing ($PERCENT%)**" README.md
fi

rm -f README.md.bak

echo "README.md updated"
