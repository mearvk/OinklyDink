package com.oinklydink.launcher;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

/**
 * The 3D pig's tail desktop widget.
 *
 * Behavior:
 *  - Sits still and comfortable by default (frame 0, no animation)
 *  - On single-click: shakes and wiggles for ~1.5 seconds, then launches
 *    the configured Java program
 *  - On double-click: shows the brilliant red key + wiggles
 *
 * The wiggle is a quick, playful burst — not continuous.
 * Careful and comfortable when at rest.
 */
public class AnimatedTailPanel extends JPanel {

    private static final int ICON_SIZE = 64;
    private static final int FPS = 24;
    private static final int FRAME_DELAY_MS = 1000 / FPS;

    /** Duration of the wiggle burst on click (milliseconds). */
    private static final int WIGGLE_DURATION_MS = 1500;

    /** Duration of the red key display (milliseconds). */
    private static final int KEY_DURATION_MS = 3000;

    private BufferedImage[] frames;
    private BufferedImage[] framesWithKey;
    private BufferedImage stillFrame;
    private int currentFrame = 0;
    private boolean showKey = false;
    private boolean wiggling = false;

    private Timer animationTimer;
    private Timer wiggleStopTimer;
    private Timer keyTimer;

    /** Callback invoked after the wiggle animation completes (launches the program). */
    private Runnable onLaunchAction;

    public AnimatedTailPanel() {
        setPreferredSize(new Dimension(ICON_SIZE + 12, ICON_SIZE + 12));
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setToolTipText("Click to launch! Double-click for the key.");

        // Pre-render all 44 frames
        frames = PigTailIcon.generateAllFrames(ICON_SIZE, false);
        framesWithKey = PigTailIcon.generateAllFrames(ICON_SIZE, true);
        stillFrame = frames[0]; // Resting position

        // Animation timer (only runs during wiggle)
        animationTimer = new Timer(FRAME_DELAY_MS, e -> {
            currentFrame = (currentFrame + 1) % PigTailIcon.TOTAL_FRAMES;
            repaint();
        });

        // Wiggle stop timer — stops animation after burst, then fires launch
        wiggleStopTimer = new Timer(WIGGLE_DURATION_MS, e -> {
            stopWiggle();
            // Fire the launch action after the wiggle completes
            if (onLaunchAction != null && !showKey) {
                onLaunchAction.run();
            }
        });
        wiggleStopTimer.setRepeats(false);

        // Key display timer
        keyTimer = new Timer(KEY_DURATION_MS, e -> {
            showKey = false;
            keyTimer.stop();
            repaint();
        });
        keyTimer.setRepeats(false);

        // Mouse interaction
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    // Double-click: show red key + wiggle (no launch)
                    activateKey();
                } else if (e.getClickCount() == 1) {
                    // Single-click: wiggle then launch
                    if (!wiggling) {
                        startWiggleAndLaunch();
                    }
                }
            }
        });
    }

    /**
     * Sets the action to run when the tail is clicked (after wiggle completes).
     */
    public void setOnLaunchAction(Runnable action) {
        this.onLaunchAction = action;
    }

    /**
     * Single-click: start a wiggle burst, then launch.
     */
    private void startWiggleAndLaunch() {
        wiggling = true;
        currentFrame = 0;
        animationTimer.start();
        wiggleStopTimer.restart();
    }

    /**
     * Double-click: show the red key and wiggle (cosmetic only).
     */
    public void activateKey() {
        showKey = true;
        wiggling = true;
        currentFrame = 0;
        animationTimer.start();
        keyTimer.restart();

        // Stop the wiggle after key display ends
        Timer keyWiggleStop = new Timer(KEY_DURATION_MS, e -> stopWiggle());
        keyWiggleStop.setRepeats(false);
        keyWiggleStop.start();
    }

    /**
     * Stops the wiggle, returns to comfortable rest.
     */
    private void stopWiggle() {
        wiggling = false;
        animationTimer.stop();
        wiggleStopTimer.stop();
        currentFrame = 0;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int x = (getWidth() - ICON_SIZE) / 2;
        int y = (getHeight() - ICON_SIZE) / 2;

        BufferedImage frame;
        if (wiggling) {
            frame = showKey ? framesWithKey[currentFrame] : frames[currentFrame];
        } else {
            frame = showKey ? framesWithKey[0] : stillFrame;
        }

        g2d.drawImage(frame, x, y, null);
        g2d.dispose();
    }

    /**
     * Returns the current frame image (for window icon sync).
     */
    public BufferedImage getCurrentFrame() {
        if (wiggling) {
            return showKey ? framesWithKey[currentFrame] : frames[currentFrame];
        }
        return stillFrame;
    }

    /**
     * Whether the tail is currently wiggling.
     */
    public boolean isWiggling() {
        return wiggling;
    }

    /**
     * Clean up timers.
     */
    public void dispose() {
        animationTimer.stop();
        wiggleStopTimer.stop();
        keyTimer.stop();
    }
}
