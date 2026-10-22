#!/usr/bin/env bash
# Optional fetch of official Lucide SVGs to supplement the bundled offline icon set.
# Run on an Internet-connected machine; check the original ISC license.
set -euo pipefail
cd "$(dirname "$0")/.."
OUT='app/src/main/assets/icons/lucide-original'
mkdir -p "$OUT"
ICONS='home search book-open bookmark compass library settings menu more-vertical arrow-left arrow-right chevron-right chevron-down download cloud-download cloud-upload heart star share-2 check check-circle plus x trash-2 pencil filter clock history globe external-link link users sun moon eye eye-off languages grid list layout-grid files folder-plus folder bookmark-plus refresh-cw play pause skip-back skip-forward volume-2 message-circle sliders-horizontal copy wifi-wifi-off shield'
for icon in $ICONS; do
  case "$icon" in wifi-wifi-off) continue;; esac
  url="https://cdn.jsdelivr.net/npm/lucide-static@0.468.0/icons/${icon}.svg"
  if curl -fLsS --connect-timeout 10 "$url" -o "$OUT/$icon.svg"; then
    echo "+ $icon"
  else rm -f "$OUT/$icon.svg"; echo "! unavailable: $icon" >&2; fi
done
printf '\nIcons: %s\n' "$OUT"
