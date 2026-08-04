# OinklyDink - The Pig's Tail Java Launcher 🐷

> *"Worth $88,000,000 or a Man and his Day."*

A clean, professional **cross-platform** desktop launcher shaped like a **Pig's Tail** that launches Java programs — and *only* Java programs.

## Features

- **44-Frame Animated Pig's Tail** — The tail wiggles continuously with smooth sinusoidal animation at 24fps
- **Brilliant Red Key** — Double-click the tail icon to reveal a glowing red key in the upper-right corner
- **Cross-Platform** — Works on Windows 7+, Linux (GNOME/KDE/etc), and macOS
- **Main Class Launch** — Configure and launch your primary Java application
- **Two Subscripts** — Run up to two additional Java processes alongside your main class
- **Java-Only Enforcement** — OinklyDink refuses to launch anything that isn't Java
- **Persistent Configuration** — Saves your launch settings between sessions
- **System Tray Integration** — Minimizes to tray with the pig tail icon
- **Live Log Output** — See stdout/stderr from all processes in real-time
- **JVM Configuration** — Custom Java path, JVM options, classpath per process
- **Native Look & Feel** — Uses system L&F on each platform for professional appearance
- **Platform-Aware Fonts** — Segoe UI (Windows), SF/Helvetica (macOS), DejaVu (Linux)

## Architecture

```
┌─────────────────────────────────────────────────┐
│          OinklyDink Launcher (Cross-Platform)   │
│   ┌─────────────┐                              │
│   │ AnimatedTail│ ← 44 frames, wiggles         │
│   │  + Red Key  │ ← appears on double-click    │
│   └─────────────┘                              │
├──────────┬────────────┬─────────────────────────┤
│   Main   │ Subscript1 │    Subscript 2          │
│  Class   │  Process   │     Process             │
├──────────┴────────────┴─────────────────────────┤
│  ProcessBuilder (platform-aware, Java-only)     │
│  Windows: cmd /c java ...                       │
│  Linux/macOS: java ... (direct)                 │
└─────────────────────────────────────────────────┘
```

## Build

Requires: Java 11+, Maven 3.6+

```bash
mvn clean package
```

This produces `target/oinklydink-launcher-1.0.0.jar`.

## Run

### Any Platform
```bash
java -jar target/oinklydink-launcher-1.0.0.jar
```

### Windows
Double-click `OinklyDink.bat`

### Linux / macOS
```bash
./oinklydink.sh
```

## Installation

### Windows
1. Build the JAR
2. Copy `oinklydink-launcher-1.0.0.jar` and `OinklyDink.bat` to your preferred location
3. Right-click `OinklyDink.bat` → Send to → Desktop (create shortcut)

### Linux
1. Build the JAR
2. Copy to `/opt/oinklydink/` (or preferred location)
3. Edit `oinklydink.desktop` with the correct path
4. Copy to `~/.local/share/applications/`

### macOS
1. Build the JAR
2. Run with `./oinklydink.sh` or create an Automator application wrapper

## Animated Icon

The pig's tail icon features a **44-frame wiggle animation**:
- Smooth sinusoidal oscillation (±15°)
- Runs at 24fps
- The window title bar icon animates in sync
- **Double-click** the icon in the header to activate the **brilliant red key** (appears for 3 seconds in the upper-right of the icon)

## Configuration

| Tab | Field | Description |
|-----|-------|-------------|
| Main Class | Main Class | Fully qualified class name (e.g., `com.example.Main`) |
| Main Class | Classpath | Classpath (uses `;` on Windows, `:` on Unix) |
| Main Class | Arguments | Command-line arguments |
| Subscript 1 | Main Class | Secondary process class |
| Subscript 2 | Main Class | Tertiary process class |
| JVM Settings | Java Path | Path to java executable (empty = system default) |
| JVM Settings | JVM Options | Options like `-Xmx512m -ea` |

## License

Proprietary. All rights reserved.
