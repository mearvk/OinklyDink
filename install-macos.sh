#!/bin/bash
# ============================================================
# OinklyDink - macOS Installer
#
# Installs OinklyDink to ~/Applications/OinklyDink with a
# proper launcher. Verifies Java is available with GUI support.
#
# Usage: ./install-macos.sh
#
# "Worth $88,000,000 or a Man and his Day."
# ============================================================

set -e

APP_NAME="OinklyDink"
VERSION="1.0.0"
JAR_NAME="oinklydink-launcher-${VERSION}.jar"
INSTALL_DIR="$HOME/Applications/OinklyDink"

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

echo ""
echo "  ╔══════════════════════════════════════════╗"
echo "  ║  OinklyDink Installer — Pig's Tail      ║"
echo "  ║  Version ${VERSION}                          ║"
echo "  ╚══════════════════════════════════════════╝"
echo ""

# --- Step 1: Check Java ---
echo "[1/4] Checking for Java..."

if ! command -v java &> /dev/null; then
    echo "    Java not found."
    echo ""
    echo "    OinklyDink requires Java 11+ with GUI support."
    echo "    Install via Homebrew:"
    echo "      brew install openjdk@17"
    echo ""
    echo "    Or download from: https://adoptium.net/"
    echo ""

    # Offer to install via Homebrew if available
    if command -v brew &> /dev/null; then
        read -p "    Install Java 17 via Homebrew now? [Y/n] " answer
        if [ "$answer" != "n" ] && [ "$answer" != "N" ]; then
            echo "    Installing..."
            brew install openjdk@17
            echo ""
            # Link it
            sudo ln -sfn "$(brew --prefix openjdk@17)/libexec/openjdk.jdk" \
                /Library/Java/JavaVirtualMachines/openjdk-17.jdk 2>/dev/null || true
        else
            exit 1
        fi
    else
        exit 1
    fi
fi

JAVA_BIN="$(which java)"
JAVA_VER=$(java -version 2>&1 | head -1)
echo "    Found: $JAVA_BIN"
echo "    $JAVA_VER"
echo ""

# --- Step 2: Find JAR ---
echo "[2/4] Locating OinklyDink JAR..."

JAR_PATH=""
if [ -f "$SCRIPT_DIR/$JAR_NAME" ]; then
    JAR_PATH="$SCRIPT_DIR/$JAR_NAME"
elif [ -f "$SCRIPT_DIR/target/$JAR_NAME" ]; then
    JAR_PATH="$SCRIPT_DIR/target/$JAR_NAME"
fi

if [ -z "$JAR_PATH" ]; then
    echo "    ERROR: Cannot find $JAR_NAME"
    echo "    Place it next to this script, or run 'mvn clean package' first."
    exit 1
fi
echo "    Found: $JAR_PATH"
echo ""

# --- Step 3: Install ---
echo "[3/4] Installing to $INSTALL_DIR..."

mkdir -p "$INSTALL_DIR"
cp "$JAR_PATH" "$INSTALL_DIR/$JAR_NAME"
echo "    Copied $JAR_NAME"

# Create launcher script
cat > "$INSTALL_DIR/oinklydink" << EOF
#!/bin/bash
exec java -jar "$INSTALL_DIR/$JAR_NAME" "\$@"
EOF
chmod +x "$INSTALL_DIR/oinklydink"
echo "    Created launcher script"

# Symlink to /usr/local/bin if possible
if [ -d "/usr/local/bin" ]; then
    ln -sf "$INSTALL_DIR/oinklydink" "/usr/local/bin/oinklydink" 2>/dev/null || true
    echo "    Linked: /usr/local/bin/oinklydink"
fi

echo ""

# --- Step 4: Create .app bundle ---
echo "[4/4] Creating application bundle..."

APP_DIR="$INSTALL_DIR/OinklyDink.app"
mkdir -p "$APP_DIR/Contents/MacOS"
mkdir -p "$APP_DIR/Contents/Resources"

# Info.plist
cat > "$APP_DIR/Contents/Info.plist" << EOF
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN"
  "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>CFBundleName</key>
    <string>OinklyDink</string>
    <key>CFBundleDisplayName</key>
    <string>OinklyDink</string>
    <key>CFBundleIdentifier</key>
    <string>com.oinklydink.launcher</string>
    <key>CFBundleVersion</key>
    <string>${VERSION}</string>
    <key>CFBundleShortVersionString</key>
    <string>${VERSION}</string>
    <key>CFBundleExecutable</key>
    <string>oinklydink</string>
    <key>CFBundlePackageType</key>
    <string>APPL</string>
    <key>LSMinimumSystemVersion</key>
    <string>10.13</string>
    <key>NSHighResolutionCapable</key>
    <true/>
</dict>
</plist>
EOF

# Executable inside .app
cat > "$APP_DIR/Contents/MacOS/oinklydink" << EOF
#!/bin/bash
exec java -jar "$INSTALL_DIR/$JAR_NAME" "\$@"
EOF
chmod +x "$APP_DIR/Contents/MacOS/oinklydink"

echo "    Created OinklyDink.app"

# Copy .app to ~/Applications for Launchpad visibility
if [ -d "$HOME/Applications" ]; then
    cp -R "$APP_DIR" "$HOME/Applications/OinklyDink.app" 2>/dev/null || true
    echo "    Copied to ~/Applications (visible in Launchpad)"
fi

echo ""
echo "  ════════════════════════════════════════════"
echo "  Installation complete!"
echo ""
echo "  Launch:"
echo "    • Double-click OinklyDink.app"
echo "    • Spotlight: type 'OinklyDink'"
echo "    • Terminal:  oinklydink"
echo ""
echo "  Location: $INSTALL_DIR"
echo "  🐷 Oink!"
echo ""

# Launch now
read -p "  Launch OinklyDink now? [Y/n] " answer
if [ "$answer" != "n" ] && [ "$answer" != "N" ]; then
    echo "  Launching..."
    open "$APP_DIR" 2>/dev/null || "$INSTALL_DIR/oinklydink" &
fi
