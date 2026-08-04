#!/bin/bash
# ============================================================
# OinklyDink - Linux Installer
#
# Installs OinklyDink and ensures GUI Java (non-headless) is available.
# Creates desktop shortcut with pig tail icon and marks it trusted.
#
# Usage: sudo ./install-linux.sh
#    or: ./install-linux.sh --user  (installs to ~/.local)
#
# "Worth $88,000,000 or a Man and his Day."
# ============================================================

set -e

APP_NAME="OinklyDink"
VERSION="1.0.0"
JAR_NAME="oinklydink-launcher-${VERSION}.jar"

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
PINK='\033[0;35m'
NC='\033[0m'

echo -e "${PINK}╔══════════════════════════════════════════════╗${NC}"
echo -e "${PINK}║   OinklyDink Installer - Pig's Tail Launcher ║${NC}"
echo -e "${PINK}║   Version ${VERSION}                              ║${NC}"
echo -e "${PINK}╚══════════════════════════════════════════════╝${NC}"
echo ""

# --- Determine install mode ---
USER_INSTALL=false
if [ "$1" = "--user" ] || [ "$(id -u)" != "0" ]; then
    USER_INSTALL=true
    INSTALL_DIR="$HOME/.local/opt/oinklydink"
    BIN_DIR="$HOME/.local/bin"
    DESKTOP_DIR="$HOME/.local/share/applications"
    ICON_BASE="$HOME/.local/share/icons/hicolor"
    echo "Installing for current user: $USER"
else
    INSTALL_DIR="/opt/oinklydink"
    BIN_DIR="/usr/local/bin"
    DESKTOP_DIR="/usr/share/applications"
    ICON_BASE="/usr/share/icons/hicolor"
    echo "Installing system-wide (root)"
fi
echo "Install directory: $INSTALL_DIR"
echo ""

# --- Step 1: Ensure GUI Java is available ---
echo -e "[1/6] ${GREEN}Checking Java GUI support...${NC}"

install_gui_java() {
    echo "    Installing full Java GUI runtime..."
    if command -v apt-get &> /dev/null; then
        if [ "$USER_INSTALL" = true ]; then
            sudo apt-get install -y openjdk-11-jre 2>&1 | grep -E "^(Setting|openjdk)" || true
        else
            apt-get install -y openjdk-11-jre 2>&1 | grep -E "^(Setting|openjdk)" || true
        fi
    elif command -v dnf &> /dev/null; then
        if [ "$USER_INSTALL" = true ]; then
            sudo dnf install -y java-11-openjdk 2>&1 | tail -3
        else
            dnf install -y java-11-openjdk 2>&1 | tail -3
        fi
    elif command -v pacman &> /dev/null; then
        if [ "$USER_INSTALL" = true ]; then
            sudo pacman -S --noconfirm jre11-openjdk 2>&1 | tail -3
        else
            pacman -S --noconfirm jre11-openjdk 2>&1 | tail -3
        fi
    else
        echo -e "    ${RED}Cannot auto-install Java. Please install a full (non-headless) JRE.${NC}"
        exit 1
    fi
}

find_gui_java() {
    local candidates=(
        "/usr/lib/jvm/java-11-openjdk-amd64/bin/java"
        "/usr/lib/jvm/java-17-openjdk-amd64/bin/java"
        "/usr/lib/jvm/java-21-openjdk-amd64/bin/java"
        "/usr/lib/jvm/java-25-openjdk-amd64/bin/java"
        "/usr/lib/jvm/java-11-openjdk/bin/java"
        "/usr/lib/jvm/java-17-openjdk/bin/java"
        "/usr/lib/jvm/java-21-openjdk/bin/java"
    )

    for java_bin in "${candidates[@]}"; do
        if [ -f "$java_bin" ]; then
            local jvm_dir="$(dirname "$(dirname "$java_bin")")"
            if [ -f "$jvm_dir/lib/libawt_xawt.so" ]; then
                echo "$java_bin"
                return 0
            fi
        fi
    done

    if command -v java &> /dev/null; then
        local default_java="$(readlink -f "$(which java)")"
        local jvm_dir="$(dirname "$(dirname "$default_java")")"
        if [ -f "$jvm_dir/lib/libawt_xawt.so" ]; then
            echo "$default_java"
            return 0
        fi
    fi

    return 1
}

JAVA_BIN=""
if JAVA_BIN=$(find_gui_java); then
    echo -e "    ${GREEN}Found GUI Java: $JAVA_BIN${NC}"
else
    echo "    No GUI-capable Java found. Installing..."
    install_gui_java
    if JAVA_BIN=$(find_gui_java); then
        echo -e "    ${GREEN}Installed GUI Java: $JAVA_BIN${NC}"
    else
        echo -e "    ${RED}Failed to find GUI Java after install.${NC}"
        exit 1
    fi
fi
echo ""

# --- Step 2: Find the JAR ---
echo -e "[2/6] ${GREEN}Locating OinklyDink JAR...${NC}"

JAR_PATH=""
if [ -f "$SCRIPT_DIR/$JAR_NAME" ]; then
    JAR_PATH="$SCRIPT_DIR/$JAR_NAME"
elif [ -f "$SCRIPT_DIR/target/$JAR_NAME" ]; then
    JAR_PATH="$SCRIPT_DIR/target/$JAR_NAME"
fi

if [ -z "$JAR_PATH" ]; then
    echo -e "    ${RED}Cannot find $JAR_NAME${NC}"
    echo "    Place it in the same directory as this script, or run 'mvn clean package' first."
    exit 1
fi
echo "    Found: $JAR_PATH"
echo ""

# --- Step 3: Copy files ---
echo -e "[3/6] ${GREEN}Installing files...${NC}"

mkdir -p "$INSTALL_DIR"
mkdir -p "$INSTALL_DIR/icons"
cp "$JAR_PATH" "$INSTALL_DIR/$JAR_NAME"
echo "    Copied $JAR_NAME -> $INSTALL_DIR/"

# --- Step 4: Export and install icons ---
echo -e "[4/6] ${GREEN}Installing pig tail icons...${NC}"

# Export icons using the JAR itself
"$JAVA_BIN" -cp "$INSTALL_DIR/$JAR_NAME" \
    com.oinklydink.launcher.IconExporter "$INSTALL_DIR/icons" 2>/dev/null

# Install into hicolor icon theme
for size in 16 24 32 48 64 128 256 512; do
    ICON_DIR="$ICON_BASE/${size}x${size}/apps"
    mkdir -p "$ICON_DIR"
    if [ -f "$INSTALL_DIR/icons/oinklydink-${size}.png" ]; then
        cp "$INSTALL_DIR/icons/oinklydink-${size}.png" "$ICON_DIR/oinklydink.png"
    fi
done

# Update icon cache
gtk-update-icon-cache -f -t "$ICON_BASE" 2>/dev/null || true
echo "    Pig tail icons installed into icon theme."
echo ""

# --- Step 5: Create launcher ---
echo -e "[5/6] ${GREEN}Creating launcher...${NC}"

mkdir -p "$BIN_DIR"
cat > "$INSTALL_DIR/oinklydink" << EOF
#!/bin/bash
exec "$JAVA_BIN" -jar "$INSTALL_DIR/$JAR_NAME" "\$@"
EOF
chmod +x "$INSTALL_DIR/oinklydink"

ln -sf "$INSTALL_DIR/oinklydink" "$BIN_DIR/oinklydink"
echo "    Created: $BIN_DIR/oinklydink"
echo ""

# --- Step 6: Desktop integration (trusted, with icon) ---
echo -e "[6/6] ${GREEN}Creating trusted desktop shortcut...${NC}"

mkdir -p "$DESKTOP_DIR"

# Write .desktop file
DESKTOP_CONTENT="[Desktop Entry]
Version=1.0
Type=Application
Name=OinklyDink
Comment=Pig's Tail Java Launcher - Launches Java and only Java
Exec=$JAVA_BIN -jar $INSTALL_DIR/$JAR_NAME
Icon=oinklydink
Terminal=false
Categories=Development;Java;
Keywords=java;launcher;pig;tail;oinklydink;
StartupWMClass=com-oinklydink-launcher-OinklyDink
StartupNotify=true"

# Install to applications menu
echo "$DESKTOP_CONTENT" > "$DESKTOP_DIR/oinklydink.desktop"
chmod +x "$DESKTOP_DIR/oinklydink.desktop"

# Install to Desktop (if it exists)
if [ -d "$HOME/Desktop" ]; then
    DESKTOP_SHORTCUT="$HOME/Desktop/oinklydink.desktop"
    echo "$DESKTOP_CONTENT" > "$DESKTOP_SHORTCUT"
    chmod +x "$DESKTOP_SHORTCUT"

    # Mark as trusted (GNOME)
    gio set "$DESKTOP_SHORTCUT" metadata::trusted true 2>/dev/null || true

    # For GNOME 43+, also need to allow launching
    dbus-launch gio set "$DESKTOP_SHORTCUT" metadata::trusted true 2>/dev/null || true

    echo "    Desktop shortcut: trusted ✓"
fi

echo "    Application menu entry created."
echo ""

# --- Done ---
echo -e "${PINK}══════════════════════════════════════════════${NC}"
echo -e "${GREEN}Installation complete!${NC}"
echo ""
echo "  Launch: oinklydink"
echo "  Or:     Double-click the desktop icon (pig tail)"
echo "  Or:     Find 'OinklyDink' in your application menu"
echo ""
echo -e "${PINK}  🐷 Oink!${NC}"
echo ""

# --- Launch now ---
echo -n "Launch OinklyDink now? [Y/n] "
read -r answer
if [ "$answer" != "n" ] && [ "$answer" != "N" ]; then
    echo "Launching..."
    nohup "$INSTALL_DIR/oinklydink" > /dev/null 2>&1 &
fi
