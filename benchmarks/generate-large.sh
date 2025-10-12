#!/bin/bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "Generating large test datasets..."

# 50MB JSON array
echo "Generating 50MB.json..."
jq -n '[range(500000) | {
  id: .,
  name: "user\(.)",
  email: "user\(.)@example.com",
  age: (. % 80) + 18,
  active: (. % 2 == 0)
}]' > "$SCRIPT_DIR/50MB.json"

# 100MB JSON array
echo "Generating 100MB.json..."
jq -n '[range(1000000) | {
  id: .,
  name: "user\(.)",
  email: "user\(.)@example.com",
  age: (. % 80) + 18,
  active: (. % 2 == 0)
}]' > "$SCRIPT_DIR/100MB.json"

echo "Done!"
ls -lh "$SCRIPT_DIR"/*.json
