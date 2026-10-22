#!/data/data/com.termux/files/usr/bin/bash
# Pushing to GitHub triggers the APK build via GitHub Actions.
set -e
cd "$(dirname "$0")/.."
if ! command -v git >/dev/null; then pkg install -y git; fi
if [ ! -d .git ]; then git init -b main; fi
git add .
git commit -m "Rebuild Manga MM from scratch" || true
cat <<'MSG'
Create an empty repository named manga-99 on github.com first.
Then run (replace YOUR_USERNAME):
  git remote add origin https://github.com/YOUR_USERNAME/manga-99.git
  git push -u origin main
GitHub > Actions > Build Manga MM APK > latest run > Artifacts.
MSG
