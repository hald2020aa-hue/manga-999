#!/usr/bin/env bash
# Official optional MIT Tabler SVG downloads; requires internet.
set -euo pipefail
cd "$(dirname "$0")/.."
mkdir -p app/src/main/assets/icons/tabler-original
for name in home book-2 bookmark search cloud-download library settings arrow-left arrow-right language globe heart player-play download folder-plus history; do
  url="https://cdn.jsdelivr.net/npm/@tabler/icons@3.46.0/icons/outline/$name.svg"
  curl --fail --location --silent --show-error "$url" -o "app/src/main/assets/icons/tabler-original/$name.svg" || rm -f "app/src/main/assets/icons/tabler-original/$name.svg"
done
