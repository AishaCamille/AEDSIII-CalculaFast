#!/bin/zsh
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")"/.. && pwd)"

echo "Project root: $ROOT_DIR"
cd "$ROOT_DIR"

echo "[1/2] Cleaning dados/ databases"
rm -rf dados || true

echo "[2/2] Cleaning build output"
rm -rf out || true

echo "Done."


