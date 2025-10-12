#!/bin/bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DATA_FILE="$SCRIPT_DIR/5MB.json"
JQ4JAVA_JAR="$SCRIPT_DIR/../jq4java-cli/target/jq4java.jar"

echo "=== jq4java Performance Benchmarks ==="
echo

if ! command -v hyperfine &> /dev/null; then
    echo "Error: hyperfine not found. Install with:"
    echo "  brew install hyperfine  (macOS)"
    echo "  cargo install hyperfine (Rust)"
    exit 1
fi

if ! command -v jq &> /dev/null; then
    echo "Error: jq not found. Install with:"
    echo "  brew install jq  (macOS)"
    exit 1
fi

if [ ! -f "$DATA_FILE" ]; then
    echo "Downloading test data..."
    curl -sL "https://microsoftedge.github.io/Demos/json-dummy-data/5MB.json" -o "$DATA_FILE"
    echo "Downloaded 5MB.json"
    echo
fi

if [ ! -f "$JQ4JAVA_JAR" ]; then
    echo "Building jq4java..."
    cd "$SCRIPT_DIR/.."
    mvn clean package -DskipTests -q
    echo "Built jq4java.jar"
    echo
fi

echo "Running benchmarks with hyperfine..."
echo

RESULTS="$SCRIPT_DIR/results.md"
echo "# jq4java Performance Benchmarks" > "$RESULTS"
echo "" >> "$RESULTS"
echo "Comparing jq4java against native jq using [hyperfine](https://github.com/sharkdp/hyperfine)." >> "$RESULTS"
echo "" >> "$RESULTS"

echo "## Identity: ." | tee -a "$RESULTS"
echo "" >> "$RESULTS"
hyperfine \
    --warmup 3 \
    --runs 10 \
    --export-markdown /tmp/bench1.md \
    "jq '.' $DATA_FILE > /dev/null" \
    "java -jar $JQ4JAVA_JAR '.' $DATA_FILE > /dev/null"
cat /tmp/bench1.md >> "$RESULTS"
echo "" >> "$RESULTS"

echo "## Field access: .[0].name" | tee -a "$RESULTS"
echo "" >> "$RESULTS"
hyperfine \
    --warmup 3 \
    --runs 10 \
    --export-markdown /tmp/bench2.md \
    "jq '.[0].name' $DATA_FILE > /dev/null" \
    "java -jar $JQ4JAVA_JAR '.[0].name' $DATA_FILE > /dev/null"
cat /tmp/bench2.md >> "$RESULTS"
echo "" >> "$RESULTS"

echo "## Array iteration: .[] | .id" | tee -a "$RESULTS"
echo "" >> "$RESULTS"
hyperfine \
    --warmup 3 \
    --runs 10 \
    --export-markdown /tmp/bench3.md \
    "jq '.[] | .id' $DATA_FILE > /dev/null" \
    "java -jar $JQ4JAVA_JAR '.[] | .id' $DATA_FILE > /dev/null"
cat /tmp/bench3.md >> "$RESULTS"
echo "" >> "$RESULTS"

echo "## Object construction: .[] | {name, lang}" | tee -a "$RESULTS"
echo "" >> "$RESULTS"
hyperfine \
    --warmup 3 \
    --runs 10 \
    --export-markdown /tmp/bench4.md \
    "jq '.[] | {name: .name, lang: .language}' $DATA_FILE > /dev/null" \
    "java -jar $JQ4JAVA_JAR '.[] | {name: .name, lang: .language}' $DATA_FILE > /dev/null"
cat /tmp/bench4.md >> "$RESULTS"
echo "" >> "$RESULTS"

if [ -f "$SCRIPT_DIR/50MB.json" ]; then
    echo "## Large file (50MB): .[0].name" | tee -a "$RESULTS"
    echo "" >> "$RESULTS"
    hyperfine \
        --warmup 2 \
        --runs 5 \
        --export-markdown /tmp/bench5.md \
        "jq '.[0].name' $SCRIPT_DIR/50MB.json > /dev/null" \
        "java -jar $JQ4JAVA_JAR '.[0].name' $SCRIPT_DIR/50MB.json > /dev/null"
    cat /tmp/bench5.md >> "$RESULTS"
    echo "" >> "$RESULTS"
fi

if [ -f "$SCRIPT_DIR/100MB.json" ]; then
    echo "## Large file (100MB): .[0].name" | tee -a "$RESULTS"
    echo "" >> "$RESULTS"
    hyperfine \
        --warmup 2 \
        --runs 5 \
        --export-markdown /tmp/bench6.md \
        "jq '.[0].name' $SCRIPT_DIR/100MB.json > /dev/null" \
        "java -jar $JQ4JAVA_JAR '.[0].name' $SCRIPT_DIR/100MB.json > /dev/null"
    cat /tmp/bench6.md >> "$RESULTS"
    echo "" >> "$RESULTS"
fi

echo
echo "Results saved to: $SCRIPT_DIR/results.md"
