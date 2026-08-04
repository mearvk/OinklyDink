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
 * It acts as a desktop icon replacement — clicking it launches the full
 * OinklyDink launcher or directly launches the configured Java programs.
 *
 * The widget:
 *  - Is undecorated (no title bar, no border)
 *  - Stays on the desktop layer (always on bottom)
 *  - Has a transparent background (just the tail floating)
 *  - Wiggles continuously with a gentle, comfortable sway
 *  - Shows "Dink 5" label below the icon
 *  - Single-click: burst wiggle + launch
 *  - Double-click: shows the red key
 *  - Right-click: context menu (open launcher, quit)
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
                } else if (e.getClickCount() == 2) {
                    // Double-click: show red key
                    showKey = true;
                    keyTimer.restart();
                } else if (e.getClickCount() == 1) {
                    // Single-click: launch
                    SwingUtilities.invokeLater(() -> launchOinklyDink());
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

        JMenuItem openItem = new JMenuItem("Open Launcher");
        openItem.addActionListener(ev -> launchFullUI());

        JMenuItem quitItem = new JMenuItem("Quit Dink 5");
        quitItem.addActionListener(ev -> System.exit(0));

        menu.add(openItem);
        menu.addSeparator();
        menu.add(quitItem);
        menu.show(getContentPane(), e.getX(), e.getY());
    }

    private void launchOinklyDink() {
        try {
            // Find our JAR and launch the full UI
            String jarPath = getJarPath();
            String javaBin = findGuiJava();

            ProcessBuilder pb = new ProcessBuilder(javaBin, "-jar", jarPath);
            pb.redirectErrorStream(true);
            pb.start();
        } catch (Exception ex) {
            System.err.println("Launch failed: " + ex.getMessage());
        }
    }

    private void launchFullUI() {
        launchOinklyDink();
    }

    private String getJarPath() {
        // Determine the JAR path from classpath
        String cp = System.getProperty("java.class.path", "");
        for (String entry : cp.split(System.getProperty("path.separator"))) {
            if (entry.contains("oinklydink-launcher")) {
                return entry;
            }
        }
        // Fallback: look relative to working directory
        String[] candidates = {
                "oinklydink-launcher-1.0.0.jar",
                "target/oinklydink-launcher-1.0.0.jar",
                System.getProperty("user.home") + "/.local/opt/oinklydink/oinklydink-launcher-1.0.0.jar"
        };
        for (String c : candidates) {
            if (new java.io.File(c).exists()) return c;
        }
        return cp; // last resort
    }

    private String findGuiJava() {
        // Check common GUI-capable Java locations
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
                // Verify it has libawt_xawt.so
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
        });
    }
}
