#!/bin/bash
# ============================================================
# OinklyDink - macOS Installer Builder
#
# Creates a native macOS .pkg installer using pkgbuild.
# Run this on macOS after building the JAR.
#
# Usage: ./build-macos-installer.sh
# Output: OinklyDink-1.0.0.pkg
# ============================================================

set -e

APP_NAME="OinklyDink"
VERSION="1.0.0"
JAR_FILE="oinklydink-launcher-${VERSION}.jar"
PKG_ID="com.oinklydink.launcher"
INSTALL_LOCATION="/Applications/OinklyDink"

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
BUILD_DIR="$SCRIPT_DIR/build"
PAYLOAD_DIR="$BUILD_DIR/payload"
SCRIPTS_DIR="$BUILD_DIR/scripts"

echo "=== Building OinklyDink macOS Installer ==="
echo "Version: $VERSION"
echo ""

# Check for JAR
JAR_PATH="$PROJECT_ROOT/target/$JAR_FILE"
if [ ! -f "$JAR_PATH" ]; then
    echo "ERROR: $JAR_PATH not found."
    echo "Run 'mvn clean package' first."
    exit 1
fi

# Clean build directory
rm -rf "$BUILD_DIR"
mkdir -p "$PAYLOAD_DIR" "$SCRIPTS_DIR"

# Copy JAR to payload
cp "$JAR_PATH" "$PAYLOAD_DIR/$JAR_FILE"

# Create launcher script
cat > "$PAYLOAD_DIR/oinklydink" << 'EOF'
#!/bin/bash
exec java -jar "/Applications/OinklyDink/oinklydink-launcher-1.0.0.jar" "$@"
EOF
chmod +x "$PAYLOAD_DIR/oinklydink"

# Create postinstall script (symlink to /usr/local/bin)
cat > "$SCRIPTS_DIR/postinstall" << 'EOF'
#!/bin/bash
mkdir -p /usr/local/bin
ln -sf "/Applications/OinklyDink/oinklydink" /usr/local/bin/oinklydink
exit 0
EOF
chmod +x "$SCRIPTS_DIR/postinstall"

# Build the .pkg
echo "Building package..."
pkgbuild \
    --root "$PAYLOAD_DIR" \
    --identifier "$PKG_ID" \
    --version "$VERSION" \
    --install-location "$INSTALL_LOCATION" \
    --scripts "$SCRIPTS_DIR" \
    "$SCRIPT_DIR/${APP_NAME}-${VERSION}.pkg"

echo ""
echo "=== Done ==="
echo "Installer: $SCRIPT_DIR/${APP_NAME}-${VERSION}.pkg"
echo "Installs to: $INSTALL_LOCATION"
echo ""
echo "Users can run: /usr/local/bin/oinklydink"
echo "Or: open /Applications/OinklyDink/oinklydink"
