#!/bin/zsh
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")"/.. && pwd)"

echo "Project root: $ROOT_DIR"
cd "$ROOT_DIR"

echo "[1/4] Cleaning dados/ databases"
rm -rf dados || true

echo "[2/4] Cleaning build output"
rm -rf out || true

echo "[3/4] Compiling sources"
javac -encoding UTF-8 -d out $(find src/java -name "*.java")

echo "[4/4] Running app"
exec java -cp out app.Principal


