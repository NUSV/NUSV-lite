#!/bin/bash
# Usage: ./scripts/deploy-sync.sh [commit message]
# 1. Builds the APK (assembleDebug)
# 2. Copies latest content.json / docs.json from assets
# 3. Copies the APK into deploy/
# 4. Stages everything for git push
# 5. (You still need to push to GitHub manually or let this script commit)

set -e

cd "$(dirname "$0")/.."

echo "==> Building APK..."
./gradlew assembleDebug

echo "==> Copying sync assets..."
cp app/src/main/assets/content.json deploy/
cp app/src/main/assets/docs.json deploy/
cp release/NUSV-LITE-v*.apk deploy/

echo "==> Generating version.json..."
VERSION=$(grep "versionName" app/build.gradle.kts | head -1 | sed 's/.*"\(.*\)".*/\1/')
APK_NAME="NUSV-LITE-v${VERSION}-alpha.apk"

cat > deploy/version.json <<EOF
{
  "latestVersion": "${VERSION}",
  "downloadUrl": "https://raw.githubusercontent.com/NUSV/nusv-lite-sync/main/${APK_NAME}",
  "changelog": "自动部署 $(date +%Y-%m-%d)"
}
EOF

echo "==> Done. deploy/ contents:"
ls -lh deploy/
echo ""
echo "Next step:"
echo "  git add deploy/"
echo "  git commit -m \"${1:-update sync files}\""
echo "  git push"
