package com.oinklydink.launcher;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

/**
 * OinklyDink Desktop Widget — An animated pig's tail that lives on the desktop.
 *
 * This creates a small, borderless, always-on-bottom window that displays
 * the 3D pig's tail with a continuous gentle wiggle animation.
 * It IS the Dink 5 launcher — clicking it launches the configured Java program.
 *
 * The widget:
 *  - Is undecorated (no title bar, no border)
 *  - Stays on the desktop layer (always on bottom)
 *  - Has a transparent background (just the tail floating)
 *  - Wiggles continuously with a gentle, comfortable sway
 *  - Shows "Dink 5" label below the icon
 *  - Single-click: launches the configured Java program
 *  - Double-click: shows the red key (cosmetic)
 *  - Triple-click: opens the configuration UI
 *  - Right-click: context menu (launch, configure, quit)
 *
 * Launch with: java -cp oinklydink-launcher-1.0.0.jar com.oinklydink.launcher.DesktopWidget
 */
public class DesktopWidget extends JWindow {

    private static final int ICON_SIZE = 80;
    private static final int WIDGET_WIDTH = 96;
    private static final int WIDGET_HEIGHT = 110;
    private static final int FPS = 24;
    private static final int FRAME_DELAY_MS = 1000 / FPS;

    private BufferedImage[] frames;
    private BufferedImage[] framesWithKey;
    private int currentFrame = 0;
    private boolean showKey = false;
    private boolean bursting = false;

    private Timer animationTimer;
    private Timer keyTimer;

    // Position tracking for dragging
    private Point dragOffset;

    public DesktopWidget() {
        // Frameless, transparent window
        setAlwaysOnTop(false);
        setBackground(new Color(0, 0, 0, 0));
        setSize(WIDGET_WIDTH, WIDGET_HEIGHT);

        // Position in lower-right area of screen (like a desktop icon)
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(screen.width - 150, screen.height - 180);

        // Pre-render frames
        frames = PigTailIcon.generateAllFrames(ICON_SIZE, false);
        framesWithKey = PigTailIcon.generateAllFrames(ICON_SIZE, true);

        // Content pane
        JPanel content = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                // Fully transparent background
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setComposite(AlphaComposite.Clear);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();

                g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

                // Draw the tail frame centered
                int x = (getWidth() - ICON_SIZE) / 2;
                BufferedImage frame = showKey ? framesWithKey[currentFrame] : frames[currentFrame];
                g2d.drawImage(frame, x, 0, null);

                // Draw "Dink 5" label below
                g2d.setFont(new Font("DejaVu Sans", Font.BOLD, 11));
                FontMetrics fm = g2d.getFontMetrics();
                String label = "Dink 5";
                int labelWidth = fm.stringWidth(label);
                int labelX = (getWidth() - labelWidth) / 2;
                int labelY = ICON_SIZE + 14;

                // Text shadow
                g2d.setColor(new Color(0, 0, 0, 150));
                g2d.drawString(label, labelX + 1, labelY + 1);

                // Text
                g2d.setColor(Color.WHITE);
                g2d.drawString(label, labelX, labelY);

                g2d.dispose();
            }
        };
        content.setOpaque(false);
        content.setPreferredSize(new Dimension(WIDGET_WIDTH, WIDGET_HEIGHT));
        setContentPane(content);

        // Gentle continuous wiggle animation
        animationTimer = new Timer(FRAME_DELAY_MS, e -> {
            currentFrame = (currentFrame + 1) % PigTailIcon.TOTAL_FRAMES;
            repaint();
        });

        // Key timer
        keyTimer = new Timer(3000, e -> {
            showKey = false;
            keyTimer.stop();
        });
        keyTimer.setRepeats(false);

        // Mouse interactions
        setupMouseHandlers(content);

        // Start the gentle wiggle
        animationTimer.start();
    }

    private void setupMouseHandlers(JPanel content) {
        content.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    showContextMenu(e);
                } else if (e.getClickCount() == 3) {
                    // Triple-click: open configuration UI
                    showKey = true;
                    keyTimer.restart();
                    SwingUtilities.invokeLater(() -> launchConfigUI());
                } else if (e.getClickCount() == 2) {
                    // Double-click: show red key (cosmetic)
                    showKey = true;
                    keyTimer.restart();
                } else if (e.getClickCount() == 1) {
                    // Single-click: launch the configured Java program
                    SwingUtilities.invokeLater(() -> launchConfiguredProgram());
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                dragOffset = e.getPoint();
            }
        });

        // Draggable
        content.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (dragOffset != null && SwingUtilities.isLeftMouseButton(e)) {
                    Point location = getLocation();
                    setLocation(
                            location.x + e.getX() - dragOffset.x,
                            location.y + e.getY() - dragOffset.y
                    );
                }
            }
        });
    }

    private void showContextMenu(MouseEvent e) {
        JPopupMenu menu = new JPopupMenu();

        JMenuItem launchItem = new JMenuItem("Launch Program");
        launchItem.addActionListener(ev -> launchConfiguredProgram());

        JMenuItem configItem = new JMenuItem("Configure...");
        configItem.addActionListener(ev -> launchConfigUI());

        JMenuItem quitItem = new JMenuItem("Quit Dink 5");
        quitItem.addActionListener(ev -> System.exit(0));

        menu.add(launchItem);
        menu.add(configItem);
        menu.addSeparator();
        menu.add(quitItem);
        menu.show(getContentPane(), e.getX(), e.getY());
    }

    /**
     * Launches the configured Java program directly (reads from preferences).
     */
    private void launchConfiguredProgram() {
        java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userNodeForPackage(OinklyDink.class);
        String mainClass = prefs.get("main.class", "");
        String classpath = prefs.get("main.classpath", "");
        String args = prefs.get("main.args", "");
        String javaPath = prefs.get("jvm.path", "");
        String jvmOpts = prefs.get("jvm.options", "");

        if (mainClass.isEmpty()) {
            // No program configured — open the config UI instead
            launchConfigUI();
            return;
        }

        try {
            String javaExe = javaPath.isEmpty() ? "java" : javaPath;

            java.util.List<String> command = new java.util.ArrayList<>();

            // On Windows, use cmd /c for proper PATH resolution
            String osName = System.getProperty("os.name", "").toLowerCase();
            if (osName.contains("win")) {
                command.add("cmd");
                command.add("/c");
            }

            command.add(javaExe);

            if (!jvmOpts.isEmpty()) {
                for (String opt : jvmOpts.split("\\s+")) {
                    command.add(opt);
                }
            }

            if (!classpath.isEmpty()) {
                command.add("-cp");
                command.add(classpath);
            }

            command.add(mainClass);

            if (!args.isEmpty()) {
                for (String arg : args.split("\\s+")) {
                    command.add(arg);
                }
            }

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            pb.start();
        } catch (Exception ex) {
            System.err.println("Launch failed: " + ex.getMessage());
            // Fall back to config UI
            launchConfigUI();
        }
    }

    /**
     * Opens the full OinklyDink configuration UI.
     */
    private void launchConfigUI() {
        try {
            String jarPath = getJarPath();
            String javaBin = findJava();

            ProcessBuilder pb = new ProcessBuilder(javaBin, "-jar", jarPath);
            pb.redirectErrorStream(true);
            pb.start();
        } catch (Exception ex) {
            System.err.println("Config UI launch failed: " + ex.getMessage());
        }
    }

    private String getJarPath() {
        // Determine the JAR path from classpath
        String cp = System.getProperty("java.class.path", "");
        for (String entry : cp.split(System.getProperty("path.separator"))) {
            if (entry.contains("oinklydink-launcher")) {
                return entry;
            }
        }
        // Fallback: look in common install locations
        String localAppData = System.getenv("LOCALAPPDATA");
        String[] candidates = {
                "oinklydink-launcher-1.0.0.jar",
                "target/oinklydink-launcher-1.0.0.jar",
                (localAppData != null ? localAppData + "\\OinklyDink\\oinklydink-launcher-1.0.0.jar" : ""),
                System.getProperty("user.home") + "/.local/opt/oinklydink/oinklydink-launcher-1.0.0.jar"
        };
        for (String c : candidates) {
            if (!c.isEmpty() && new java.io.File(c).exists()) return c;
        }
        return cp; // last resort
    }

    private String findJava() {
        // Check configured Java path first
        java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userNodeForPackage(OinklyDink.class);
        String configuredPath = prefs.get("jvm.path", "");
        if (!configuredPath.isEmpty() && new java.io.File(configuredPath).exists()) {
            return configuredPath;
        }

        // On Windows, just use "java" (relies on PATH)
        String osName = System.getProperty("os.name", "").toLowerCase();
        if (osName.contains("win")) {
            return "java";
        }

        // On Linux, check for GUI-capable Java
        String[] candidates = {
                "/usr/lib/jvm/java-11-openjdk-amd64/bin/java",
                "/usr/lib/jvm/java-17-openjdk-amd64/bin/java",
                "/usr/lib/jvm/java-21-openjdk-amd64/bin/java",
                "/usr/lib/jvm/java-11-openjdk/bin/java",
                "/usr/lib/jvm/java-17-openjdk/bin/java",
        };
        for (String c : candidates) {
            java.io.File f = new java.io.File(c);
            if (f.exists()) {
                java.io.File awt = new java.io.File(f.getParentFile().getParent(), "lib/libawt_xawt.so");
                if (awt.exists()) return c;
            }
        }
        return "java"; // fallback
    }

    public static void main(String[] args) {
        // Ensure we can do transparent windows
        System.setProperty("sun.java2d.noddraw", "true");

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            DesktopWidget widget = new DesktopWidget();
            widget.setVisible(true);

            // On first run (no program configured), auto-open the config UI
            java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userNodeForPackage(OinklyDink.class);
            String mainClass = prefs.get("main.class", "");
            if (mainClass.isEmpty()) {
                widget.launchConfigUI();
            }
        });
    }
}
