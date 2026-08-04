#!/bin/bash
# ============================================================
# OinklyDink - macOS Native Installer Builder
#
# Produces:
#   OinklyDink-1.0.0.dmg  (drag-to-Applications disk image)
#   OinklyDink.app         (native .app bundle)
#
# Requirements:
#   - macOS 10.15+ with JDK 14+ (jpackage)
#   - Xcode Command Line Tools
#
# Run from project root after: mvn clean package
# ============================================================

set -e

APP_NAME="OinklyDink"
APP_VERSION="1.0.0"
MAIN_JAR="oinklydink-launcher-${APP_VERSION}.jar"
MAIN_CLASS="com.oinklydink.launcher.OinklyDink"

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"

echo "=== Building ${APP_NAME} macOS Installer ==="
echo "Project: $PROJECT_ROOT"
echo ""

# Check for jpackage
if ! command -v jpackage &> /dev/null; then
    echo "ERROR: jpackage not found. Requires JDK 14+."
    exit 1
fi

# Check for source JAR
JAR_PATH="$PROJECT_ROOT/target/$MAIN_JAR"
if [ ! -f "$JAR_PATH" ]; then
    echo "ERROR: $JAR_PATH not found."
    echo "Run: mvn clean package"
    exit 1
fi

# Prepare input
mkdir -p "$PROJECT_ROOT/dist/input"
cp "$JAR_PATH" "$PROJECT_ROOT/dist/input/"

# --- Build .app image ---
echo "Building application image (.app)..."
jpackage \
  --type app-image \
  --input "$PROJECT_ROOT/dist/input" \
  --name "$APP_NAME" \
  --main-jar "$MAIN_JAR" \
  --main-class "$MAIN_CLASS" \
  --app-version "$APP_VERSION" \
  --description "Pig's Tail Java Launcher" \
  --vendor "OinklyDink" \
  --mac-package-name "$APP_NAME" \
  --dest "$PROJECT_ROOT/dist/macos"

echo "Created: dist/macos/${APP_NAME}.app"
echo ""

# --- Build .dmg ---
echo "Building disk image (.dmg)..."
jpackage \
  --type dmg \
  --input "$PROJECT_ROOT/dist/input" \
  --name "$APP_NAME" \
  --main-jar "$MAIN_JAR" \
  --main-class "$MAIN_CLASS" \
  --app-version "$APP_VERSION" \
  --description "Pig's Tail Java Launcher" \
  --vendor "OinklyDink" \
  --mac-package-name "$APP_NAME" \
  --dest "$PROJECT_ROOT/dist/macos"

echo ""
echo "=== Done ==="
echo ""
echo "Outputs in: $PROJECT_ROOT/dist/macos/"
ls -lh "$PROJECT_ROOT/dist/macos/"
echo ""
echo "To install:"
echo "  1. Open ${APP_NAME}-${APP_VERSION}.dmg"
echo "  2. Drag ${APP_NAME} to Applications"
echo "  3. Launch from Launchpad or Spotlight"
