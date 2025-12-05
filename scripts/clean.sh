#!/bin/zsh
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")"/.. && pwd)"
DATA_DIR="$ROOT_DIR/demo/dados"

echo "Project root: $ROOT_DIR"

if [[ -d "$DATA_DIR" ]]; then
  echo "Deleting data directory: $DATA_DIR"
  rm -rf "$DATA_DIR"
  echo "Data directory removed."
else
  echo "No data directory found at: $DATA_DIR (nothing to clean)"
fi

echo "Tip: restart the backend to recreate data files on demand."
