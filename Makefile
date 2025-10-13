# jq4java - Git Worktrees Management
.PHONY: help worktrees-list worktrees-build worktrees-clean build test clean

# Default target
help:
	@echo "🌳 jq4java - Git Worktrees & Build Commands"
	@echo "============================================="
	@echo ""
	@echo "Worktrees:"
	@echo "  make worktree-create FEATURE=<name>  Create new feature worktree"
	@echo "  make worktree-list                   List all worktrees with status"
	@echo "  make worktree-remove FEATURE=<name>  Remove feature worktree"
	@echo "  make worktrees-build                 Build all worktrees in parallel"
	@echo "  make worktrees-clean                 Clean all worktree build artifacts"
	@echo ""
	@echo "Development:"
	@echo "  make build                           Build current worktree"
	@echo "  make test                            Run tests in current worktree"
	@echo "  make clean                           Clean current worktree"
	@echo ""
	@echo "Examples:"
	@echo "  make worktree-create FEATURE=new-builtin"
	@echo "  make worktree-remove FEATURE=old-feature"

# Worktree management
worktree-create:
	@if [ -z "$(FEATURE)" ]; then \
		echo "❌ Usage: make worktree-create FEATURE=<name>"; \
		echo "Example: make worktree-create FEATURE=new-builtin"; \
		exit 1; \
	fi
	@echo "🌳 Creating feature worktree: $(FEATURE)"
	@BRANCH_NAME="feature/$(FEATURE)"; \
	WORKTREE_PATH="../jq4java-worktrees/features/$(FEATURE)"; \
	if git show-ref --verify --quiet refs/heads/$$BRANCH_NAME; then \
		echo "❌ Branch '$$BRANCH_NAME' already exists!"; \
		exit 1; \
	fi; \
	mkdir -p ../jq4java-worktrees/features; \
	git worktree add -b $$BRANCH_NAME $$WORKTREE_PATH main; \
	echo "✅ Feature worktree created!"; \
	echo "📁 Path: $$WORKTREE_PATH"; \
	echo "🌿 Branch: $$BRANCH_NAME"; \
	echo ""; \
	echo "Next steps:"; \
	echo "  cd $$WORKTREE_PATH"; \
	echo "  # Start developing your feature"

worktree-list:
	@echo "🌳 Git Worktrees Overview"
	@echo "========================="
	@echo ""
	@git worktree list | while IFS= read -r line; do \
		path=$$(echo "$$line" | awk '{print $$1}'); \
		commit=$$(echo "$$line" | awk '{print $$2}'); \
		branch=$$(echo "$$line" | sed 's/.*\[\(.*\)\].*/\1/'); \
		if [ "$$path" = "$$(pwd)" ]; then \
			status="📍 CURRENT"; \
			rel_path="."; \
		else \
			rel_path=$$(realpath --relative-to="$$(pwd)" "$$path" 2>/dev/null || echo "$$path"); \
			status="📁"; \
		fi; \
		echo "$$status $$branch"; \
		echo "   Path: $$rel_path"; \
		echo "   Commit: $$commit"; \
		if [ -d "$$path" ]; then \
			cd "$$path"; \
			if ! git diff-index --quiet HEAD 2>/dev/null; then \
				echo "   Status: ⚠️  Has uncommitted changes"; \
			else \
				echo "   Status: ✅ Clean"; \
			fi; \
			cd - > /dev/null; \
		fi; \
		echo ""; \
	done

worktree-remove:
	@if [ -z "$(FEATURE)" ]; then \
		echo "❌ Usage: make worktree-remove FEATURE=<name>"; \
		echo "Example: make worktree-remove FEATURE=old-feature"; \
		echo ""; \
		echo "Available worktrees:"; \
		git worktree list; \
		exit 1; \
	fi
	@BRANCH_NAME="feature/$(FEATURE)"; \
	WORKTREE_PATH="../jq4java-worktrees/features/$(FEATURE)"; \
	echo "🗑️  Removing feature worktree: $(FEATURE)"; \
	if [ ! -d "$$WORKTREE_PATH" ]; then \
		echo "❌ Worktree directory not found: $$WORKTREE_PATH"; \
		exit 1; \
	fi; \
	cd "$$WORKTREE_PATH"; \
	if ! git diff-index --quiet HEAD 2>/dev/null; then \
		echo "⚠️  Warning: Worktree has uncommitted changes!"; \
		git status --short; \
		echo ""; \
		echo "❌ Please commit or stash changes first"; \
		exit 1; \
	fi; \
	cd - > /dev/null; \
	git worktree remove "$$WORKTREE_PATH"; \
	echo "✅ Worktree removed successfully!"

worktrees-build:
	@echo "🏗️  Building all worktrees in parallel..."
	@echo "========================================"
	@echo ""
	@git worktree list | while IFS= read -r line; do \
		path=$$(echo "$$line" | awk '{print $$1}'); \
		branch=$$(echo "$$line" | sed 's/.*\[\(.*\)\].*/\1/'); \
		name=$$(basename "$$path"); \
		echo "[$$name] Starting build..."; \
		cd "$$path"; \
		if mvn clean test -q > "/tmp/jq4java-build-$$name.log" 2>&1; then \
			echo "[$$name] ✅ Build successful"; \
		else \
			echo "[$$name] ❌ Build failed - check /tmp/jq4java-build-$$name.log"; \
		fi & \
	done; \
	wait; \
	echo ""; \
	echo "✅ All builds completed!"

worktrees-clean:
	@echo "🧹 Cleaning all worktrees..."
	@git worktree list | while IFS= read -r line; do \
		path=$$(echo "$$line" | awk '{print $$1}'); \
		name=$$(basename "$$path"); \
		echo "[$$name] Cleaning..."; \
		cd "$$path" && mvn clean -q; \
	done
	@echo "✅ All worktrees cleaned!"

# Development commands for current worktree
build:
	@echo "🏗️  Building current worktree..."
	@mvn clean package

test:
	@echo "🧪 Running tests..."
	@mvn test

clean:
	@echo "🧹 Cleaning..."
	@mvn clean