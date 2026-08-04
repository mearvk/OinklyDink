#!/bin/bash
# ============================================================
# OinklyDink - Linux Native Package Builder
#
# Produces:
#   oinklydink_1.0.0_amd64.deb  (Debian/Ubuntu/Mint)
#   oinklydink-1.0.0.x86_64.rpm (Fedora/RHEL/CentOS)
#
# Requirements:
#   - JDK 14+ with jpackage
#   - dpkg-deb (for .deb) or rpmbuild (for .rpm)
#
# Run from project root after: mvn clean package
# ============================================================

set -e

APP_NAME="OinklyDink"
APP_NAME_LOWER="oinklydink"
APP_VERSION="1.0.0"
MAIN_JAR="oinklydink-launcher-${APP_VERSION}.jar"
MAIN_CLASS="com.oinklydink.launcher.OinklyDink"

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"

echo "=== Building ${APP_NAME} Linux Packages ==="
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

# --- Build .deb ---
if command -v dpkg-deb &> /dev/null; then
    echo "Building .deb package..."
    jpackage \
      --type deb \
      --input "$PROJECT_ROOT/dist/input" \
      --name "$APP_NAME" \
      --main-jar "$MAIN_JAR" \
      --main-class "$MAIN_CLASS" \
      --app-version "$APP_VERSION" \
      --description "Pig's Tail Java Launcher - Launches Java programs and only Java programs" \
      --vendor "OinklyDink" \
      --linux-shortcut \
      --linux-menu-group "Development" \
      --linux-deb-maintainer "oinklydink@oinklydink.com" \
      --linux-app-category "Development" \
      --dest "$PROJECT_ROOT/dist/linux"

    echo "Created .deb in dist/linux/"
    echo ""
else
    echo "SKIP: dpkg-deb not found, skipping .deb"
fi

# --- Build .rpm ---
if command -v rpmbuild &> /dev/null; then
    echo "Building .rpm package..."
    jpackage \
      --type rpm \
      --input "$PROJECT_ROOT/dist/input" \
      --name "$APP_NAME" \
      --main-jar "$MAIN_JAR" \
      --main-class "$MAIN_CLASS" \
      --app-version "$APP_VERSION" \
      --description "Pig's Tail Java Launcher - Launches Java programs and only Java programs" \
      --vendor "OinklyDink" \
      --linux-shortcut \
      --linux-menu-group "Development" \
      --linux-app-category "Development" \
      --linux-rpm-license-type "Proprietary" \
      --dest "$PROJECT_ROOT/dist/linux"

    echo "Created .rpm in dist/linux/"
    echo ""
else
    echo "SKIP: rpmbuild not found, skipping .rpm"
fi

echo "=== Done ==="
echo ""
echo "Outputs:"
ls -lh "$PROJECT_ROOT/dist/linux/" 2>/dev/null
echo ""
echo "Install:"
echo "  Debian/Ubuntu: sudo dpkg -i dist/linux/${APP_NAME_LOWER}_${APP_VERSION}*.deb"
echo "  Fedora/RHEL:   sudo rpm -i dist/linux/${APP_NAME}-${APP_VERSION}*.rpm"
