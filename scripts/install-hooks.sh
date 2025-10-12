#!/bin/bash

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
HOOK_DIR="$SCRIPT_DIR/../.git/hooks"

echo "Installing git hooks..."

# Create pre-push hook
cat > "$HOOK_DIR/pre-push" << 'EOF'
#!/bin/bash

echo "Updating README stats..."
bash scripts/update-readme-stats.sh

# Stage README if it changed
if ! git diff --quiet README.md; then
    git add README.md
    echo "README.md updated with test stats"
fi

exit 0
EOF

chmod +x "$HOOK_DIR/pre-push"

echo "✓ pre-push hook installed"
echo ""
echo "The hook will automatically update README.md with test stats before each push."
echo "To uninstall: rm .git/hooks/pre-push"
