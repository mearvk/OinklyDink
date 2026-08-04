#!/bin/bash
# ============================================================
# OinklyDink - Pig's Tail Java Launcher
# Cross-platform launcher script (Linux/macOS)
#
# "Worth $88,000,000 or a Man and his Day."
# ============================================================

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
JAR_NAME="oinklydink-launcher-1.0.0.jar"

# Check for Java
if ! command -v java &> /dev/null; then
    echo "ERROR: Java not found on PATH."
    echo "OinklyDink launches Java programs and ONLY Java programs."
    echo "Please install Java 11+ and ensure it is on your PATH."
    exit 1
fi

# Check Java version (need 11+)
JAVA_VER=$(java -version 2>&1 | head -1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VER" -lt 11 ] 2>/dev/null; then
    echo "WARNING: Java 11+ recommended. Detected version: $JAVA_VER"
fi

# Find the JAR - check current dir, then target/
JAR_FILE=""
if [ -f "$SCRIPT_DIR/$JAR_NAME" ]; then
    JAR_FILE="$SCRIPT_DIR/$JAR_NAME"
elif [ -f "$SCRIPT_DIR/target/$JAR_NAME" ]; then
    JAR_FILE="$SCRIPT_DIR/target/$JAR_NAME"
fi

if [ -z "$JAR_FILE" ]; then
    echo "ERROR: Cannot find $JAR_NAME"
    echo "Run 'mvn clean package' first to build OinklyDink."
    exit 1
fi

# Launch OinklyDink
exec java -jar "$JAR_FILE" "$@"
