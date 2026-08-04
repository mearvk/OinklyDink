package com.oinklydink.launcher;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

/**
 * Animated icon panel that displays the pig's tail with a 44-frame wiggle animation.
 * On double-click, shows a brilliant red key in the upper-right corner.
 *
 * The animation runs at ~24fps for smooth wiggling.
 */
public class AnimatedTailPanel extends JPanel {

    private static final int ICON_SIZE = 64;
    private static final int FPS = 24;
    private static final int FRAME_DELAY_MS = 1000 / FPS;

    private BufferedImage[] frames;
    private BufferedImage[] framesWithKey;
    private int currentFrame = 0;
    private boolean showKey = false;
    private boolean animating = false;
    private Timer animationTimer;

    // Key visibility timer (shows for 3 seconds after double-click)
    private Timer keyTimer;

    public AnimatedTailPanel() {
        setPreferredSize(new Dimension(ICON_SIZE + 8, ICON_SIZE + 8));
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setToolTipText("Double-click to activate the key!");

        // Pre-render all 44 frames (with and without key)
        frames = PigTailIcon.generateAllFrames(ICON_SIZE, false);
        framesWithKey = PigTailIcon.generateAllFrames(ICON_SIZE, true);

        // Animation timer
        animationTimer = new Timer(FRAME_DELAY_MS, e -> {
            currentFrame = (currentFrame + 1) % PigTailIcon.TOTAL_FRAMES;
            repaint();
        });

        // Key display timer - hides key after 3 seconds
        keyTimer = new Timer(3000, e -> {
            showKey = false;
            keyTimer.stop();
            repaint();
        });
        keyTimer.setRepeats(false);

        // Double-click handler - shows the red key and triggers wiggle
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    activateKey();
                }
            }
        });

        // Start animating
        startAnimation();
    }

    /**
     * Activates the red key display and triggers an animation burst.
     */
    public void activateKey() {
        showKey = true;
        keyTimer.restart();

        // Ensure animation is running during key display
        if (!animating) {
            startAnimation();
        }

        repaint();
    }

    /**
     * Starts the wiggle animation.
     */
    public void startAnimation() {
        animating = true;
        animationTimer.start();
    }

    /**
     * Stops the wiggle animation, leaving it at the current frame.
     */
    public void stopAnimation() {
        animating = false;
        animationTimer.stop();
    }

    /**
     * Returns whether the animation is currently running.
     */
    public boolean isAnimating() {
        return animating;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        // Center the icon in the panel
        int x = (getWidth() - ICON_SIZE) / 2;
        int y = (getHeight() - ICON_SIZE) / 2;

        BufferedImage frame = showKey ? framesWithKey[currentFrame] : frames[currentFrame];
        g2d.drawImage(frame, x, y, null);

        g2d.dispose();
    }

    /**
     * Gets the current rendered frame (useful for setting as window icon).
     */
    public BufferedImage getCurrentFrame() {
        return showKey ? framesWithKey[currentFrame] : frames[currentFrame];
    }

    /**
     * Cleans up timers when the panel is removed.
     */
    public void dispose() {
        animationTimer.stop();
        keyTimer.stop();
    }
}
