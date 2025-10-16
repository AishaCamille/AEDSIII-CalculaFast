#!/bin/zsh
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")"/.. && pwd)"

echo "Project root: $ROOT_DIR"
cd "$ROOT_DIR"

echo "[1/3] Creating out directory"
mkdir -p out

echo "[2/3] Compiling sources"
javac -encoding UTF-8 -d out $(find src/java -name "*.java")

echo "[3/3] Running app"
exec java -cp out app.Principal


