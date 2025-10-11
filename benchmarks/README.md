# Performance Benchmarks

This directory contains scripts to benchmark jq4java against native jq.

## Requirements

- [hyperfine](https://github.com/sharkdp/hyperfine) - Command-line benchmarking tool
- [jq](https://jqlang.github.io/jq/) - Native jq installation

### Install on macOS

```bash
brew install hyperfine jq
```

### Install on Linux

```bash
# Ubuntu/Debian
sudo apt install jq
cargo install hyperfine

# Or download hyperfine binary from GitHub releases
```

## Running Benchmarks

```bash
cd benchmarks
./run-benchmarks.sh
```

The script will:
1. Download test data (5MB.json) if not present
2. Build jq4java if needed
3. Run benchmarks with warmup and multiple iterations
4. Save results to `results.md`

## Benchmark Queries

- **Identity**: `.`
- **Field access**: `.[0].name`
- **Array iteration**: `.[] | .id`
- **Object construction**: `.[] | {name: .name, lang: .language}`

## GitHub Actions

To run in CI, add hyperfine and jq to your workflow:

```yaml
- name: Install dependencies
  run: |
    brew install hyperfine jq  # macOS
    # or
    sudo apt install jq && cargo install hyperfine  # Linux

- name: Run benchmarks
  run: ./benchmarks/run-benchmarks.sh
```
