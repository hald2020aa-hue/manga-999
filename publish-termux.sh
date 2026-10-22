#!/data/data/com.termux/files/usr/bin/bash
set -Eeuo pipefail

REPO="manga-999"
EXPECTED_OWNER="hald2020aa-hue"
cd "$(dirname "$0")"
printf '\nManga MM: تجهيز مستودع %s/%s\n' "$EXPECTED_OWNER" "$REPO"

if ! command -v git >/dev/null 2>&1 || ! command -v gh >/dev/null 2>&1; then
  echo 'أولًا ثبّت: pkg install git gh -y'
  exit 1
fi

if ! gh auth status >/dev/null 2>&1; then
  echo 'سجّل الدخول إلى GitHub من المتصفح، ثم أكمل التعليمات في Termux.'
  gh auth login --hostname github.com --git-protocol https --web
fi
OWNER="$(gh api user --jq .login)"
if [ "$OWNER" != "$EXPECTED_OWNER" ]; then
  printf 'الحساب الحالي %s، بينما المستودع المطلوب في %s.\n' "$OWNER" "$EXPECTED_OWNER"
  echo 'بدّل الحساب من gh auth switch، ثم أعد التشغيل.'
  exit 2
fi

if [ ! -d .git ]; then
  git init -b main
fi
git branch -M main
if [ -z "$(git config user.name || true)" ]; then git config user.name "$OWNER"; fi
if [ -z "$(git config user.email || true)" ]; then
  git config user.email "$(gh api user --jq '.id')+$OWNER@users.noreply.github.com"
fi
git add -A
if ! git diff --cached --quiet; then
  git commit -m 'Initialize Manga MM Android app and resources'
fi

if gh repo view "$EXPECTED_OWNER/$REPO" >/dev/null 2>&1; then
  echo 'المستودع موجود؛ سيتم الرفع إليه.'
  if git remote get-url origin >/dev/null 2>&1; then
    git remote set-url origin "https://github.com/$EXPECTED_OWNER/$REPO.git"
  else
    git remote add origin "https://github.com/$EXPECTED_OWNER/$REPO.git"
  fi
  git push -u origin main
else
  echo 'المستودع غير موجود؛ سيتم إنشاؤه خاصًا (Private).'
  gh repo create "$EXPECTED_OWNER/$REPO" --private --source=. --remote=origin --push
fi
printf '\nتمّ النشر إن لم تظهر أخطاء أعلاه: https://github.com/%s/%s\n' "$EXPECTED_OWNER" "$REPO"
echo 'افتح Actions بعد ذلك لمراجعة بناء APK.'
