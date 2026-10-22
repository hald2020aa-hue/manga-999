#!/usr/bin/env bash
# Portable bootstrap: uses installed Gradle or downloads official Gradle 8.9.
# GitHub Actions uses setup-gradle and does not need this script.
set -euo pipefail
here="$(cd "$(dirname "$0")" && pwd)"
if [ -f "$here/gradle/wrapper/gradle-wrapper.jar" ]; then
  exec java -cp "$here/gradle/wrapper/gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain "$@"
fi
if command -v gradle >/dev/null 2>&1; then exec gradle "$@"; fi
cache="${GRADLE_USER_HOME:-$HOME/.gradle}/manga-mm-bootstrap"
if [ ! -x "$cache/gradle-8.9/bin/gradle" ]; then
  mkdir -p "$cache"
  echo "Downloading official Gradle 8.9..." >&2
  curl -fLsS --retry 2 https://services.gradle.org/distributions/gradle-8.9-bin.zip -o "$cache/gradle-8.9-bin.zip"
  unzip -q -o "$cache/gradle-8.9-bin.zip" -d "$cache"
fi
exec "$cache/gradle-8.9/bin/gradle" "$@"
