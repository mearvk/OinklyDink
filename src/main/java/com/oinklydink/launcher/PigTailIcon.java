package com.oinklydink.launcher;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;

/**
 * Generates a clean, professional pig's tail (curly/spiral) icon
 * with support for 44-frame animation (wiggle) and a brilliant red key overlay.
 *
 * The tail is rendered as an elegant spiral with a subtle pink gradient,
 * clean anti-aliased lines, and professional presentation.
 */
public class PigTailIcon {

    // Pig pink color palette
    private static final Color TAIL_PRIMARY = new Color(0xC0, 0x5E, 0x7A);
    private static final Color TAIL_SECONDARY = new Color(0xE8, 0x8E, 0xA5);
    private static final Color TAIL_HIGHLIGHT = new Color(0xF5, 0xC6, 0xD4);
    private static final Color TAIL_SHADOW = new Color(0x8B, 0x3A, 0x56);
    private static final Color BACKGROUND = new Color(0xFA, 0xF0, 0xF2);

    // Red key colors
    private static final Color KEY_RED = new Color(0xE0, 0x1B, 0x1B);
    private static final Color KEY_RED_BRIGHT = new Color(0xFF, 0x33, 0x33);
    private static final Color KEY_RED_DARK = new Color(0x99, 0x10, 0x10);

    /** Total animation frames for the wiggle cycle. */
    public static final int TOTAL_FRAMES = 44;

    /**
     * Creates a static pig tail icon image at the specified size.
     */
    public static BufferedImage createPigTailImage(int size) {
        return createAnimatedFrame(size, 0, false);
    }

    /**
     * Creates a single frame of the 44-frame animated pig tail.
     *
     * @param size      Icon size in pixels
     * @param frame     Frame index (0-43)
     * @param showKey   Whether to render the brilliant red key in the upper-right
     * @return Rendered frame as a BufferedImage
     */
    public static BufferedImage createAnimatedFrame(int size, int frame, boolean showKey) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        // High quality rendering
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

        float scale = size / 64.0f;

        // Background circle (subtle)
        g2d.setColor(BACKGROUND);
        g2d.fillOval(1, 1, size - 2, size - 2);

        // Draw a thin border
        g2d.setColor(new Color(0xDD, 0xBB, 0xC5));
        g2d.setStroke(new BasicStroke(scale * 1.0f));
        g2d.drawOval(1, 1, size - 3, size - 3);

        // Calculate wiggle angle for this frame
        // Sinusoidal wiggle: oscillates between -15° and +15°
        double wiggleAngle = Math.sin(2.0 * Math.PI * frame / TOTAL_FRAMES) * 15.0;

        // Draw the pig's tail spiral with wiggle rotation
        drawPigTail(g2d, size, scale, wiggleAngle);

        // Draw the red key in upper-right if activated
        if (showKey) {
            drawRedKey(g2d, size, scale);
        }

        g2d.dispose();
        return image;
    }

    /**
     * Generates all 44 frames of the animation.
     *
     * @param size    Icon size in pixels
     * @param showKey Whether to include the red key
     * @return Array of 44 BufferedImages
     */
    public static BufferedImage[] generateAllFrames(int size, boolean showKey) {
        BufferedImage[] frames = new BufferedImage[TOTAL_FRAMES];
        for (int i = 0; i < TOTAL_FRAMES; i++) {
            frames[i] = createAnimatedFrame(size, i, showKey);
        }
        return frames;
    }

    /**
     * Draws the signature pig's tail — a clean, elegant spiral curl.
     * Applies a wiggle rotation around the center.
     */
    private static void drawPigTail(Graphics2D g2d, int size, float scale, double wiggleDegrees) {
        float cx = size / 2.0f;
        float cy = size / 2.0f;

        // Save transform and apply wiggle rotation around center
        AffineTransform originalTransform = g2d.getTransform();
        g2d.rotate(Math.toRadians(wiggleDegrees), cx, cy);

        // Create the spiral path using a parametric curve
        GeneralPath tailPath = new GeneralPath();

        // Starting point (base of tail, bottom-left)
        float startX = cx - 12 * scale;
        float startY = cy + 14 * scale;

        tailPath.moveTo(startX, startY);

        // First curl (largest) - going up-right
        tailPath.curveTo(
                cx - 14 * scale, cy + 2 * scale,
                cx - 4 * scale, cy - 12 * scale,
                cx + 8 * scale, cy - 10 * scale
        );

        // Second curl - going right-down
        tailPath.curveTo(
                cx + 18 * scale, cy - 8 * scale,
                cx + 18 * scale, cy + 4 * scale,
                cx + 8 * scale, cy + 6 * scale
        );

        // Third curl (tightest) - going left-up
        tailPath.curveTo(
                cx + 0 * scale, cy + 8 * scale,
                cx - 2 * scale, cy + 0 * scale,
                cx + 4 * scale, cy - 2 * scale
        );

        // Final tight curl tip
        tailPath.curveTo(
                cx + 9 * scale, cy - 4 * scale,
                cx + 9 * scale, cy + 1 * scale,
                cx + 5 * scale, cy + 1 * scale
        );

        // Draw shadow
        AffineTransform shadowTransform = AffineTransform.getTranslateInstance(scale * 1.2, scale * 1.2);
        Shape shadowShape = shadowTransform.createTransformedShape(tailPath);
        g2d.setColor(new Color(0, 0, 0, 30));
        g2d.setStroke(new BasicStroke(scale * 4.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.draw(shadowShape);

        // Draw outer stroke (dark outline)
        g2d.setColor(TAIL_SHADOW);
        g2d.setStroke(new BasicStroke(scale * 5.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.draw(tailPath);

        // Draw main tail body with gradient
        GradientPaint gradient = new GradientPaint(
                cx - 10 * scale, cy - 10 * scale, TAIL_SECONDARY,
                cx + 10 * scale, cy + 10 * scale, TAIL_PRIMARY
        );
        g2d.setPaint(gradient);
        g2d.setStroke(new BasicStroke(scale * 3.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.draw(tailPath);

        // Draw highlight (inner thin line for 3D effect)
        g2d.setColor(TAIL_HIGHLIGHT);
        g2d.setStroke(new BasicStroke(scale * 1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        AffineTransform highlightTransform = AffineTransform.getTranslateInstance(-scale * 0.5, -scale * 0.8);
        Shape highlightShape = highlightTransform.createTransformedShape(tailPath);
        g2d.draw(highlightShape);

        // Restore transform
        g2d.setTransform(originalTransform);
    }

    /**
     * Draws a brilliant red key icon in the upper-right corner of the icon.
     * The key represents activation/unlocking.
     */
    private static void drawRedKey(Graphics2D g2d, int size, float scale) {
        float keyX = size * 0.65f;
        float keyY = size * 0.08f;
        float keyScale = scale * 0.7f;

        // Key head (circular ring)
        float headCx = keyX + 6 * keyScale;
        float headCy = keyY + 6 * keyScale;
        float headRadius = 5 * keyScale;

        // Glow effect
        g2d.setColor(new Color(0xFF, 0x33, 0x33, 80));
        g2d.fill(new Ellipse2D.Float(
                headCx - headRadius - 2 * keyScale,
                headCy - headRadius - 2 * keyScale,
                (headRadius + 2 * keyScale) * 2,
                (headRadius + 2 * keyScale) * 2));

        // Key head outer
        GradientPaint keyGradient = new GradientPaint(
                headCx - headRadius, headCy - headRadius, KEY_RED_BRIGHT,
                headCx + headRadius, headCy + headRadius, KEY_RED_DARK);
        g2d.setPaint(keyGradient);
        g2d.fill(new Ellipse2D.Float(
                headCx - headRadius, headCy - headRadius,
                headRadius * 2, headRadius * 2));

        // Key head hole
        float holeRadius = 2 * keyScale;
        g2d.setColor(BACKGROUND);
        g2d.fill(new Ellipse2D.Float(
                headCx - holeRadius, headCy - holeRadius,
                holeRadius * 2, holeRadius * 2));

        // Key shaft
        g2d.setColor(KEY_RED);
        g2d.setStroke(new BasicStroke(1.8f * keyScale, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        float shaftStartX = headCx;
        float shaftStartY = headCy + headRadius;
        float shaftEndY = shaftStartY + 10 * keyScale;
        g2d.draw(new Line2D.Float(shaftStartX, shaftStartY, shaftStartX, shaftEndY));

        // Key teeth
        g2d.setStroke(new BasicStroke(1.5f * keyScale, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER));
        float toothLen = 3 * keyScale;
        g2d.draw(new Line2D.Float(shaftStartX, shaftEndY - 3 * keyScale,
                shaftStartX + toothLen, shaftEndY - 3 * keyScale));
        g2d.draw(new Line2D.Float(shaftStartX, shaftEndY - 1 * keyScale,
                shaftStartX + toothLen * 0.7f, shaftEndY - 1 * keyScale));

        // Bright specular highlight on key head
        g2d.setColor(new Color(0xFF, 0xFF, 0xFF, 160));
        g2d.fill(new Ellipse2D.Float(
                headCx - headRadius * 0.4f,
                headCy - headRadius * 0.6f,
                headRadius * 0.5f,
                headRadius * 0.4f));
    }

    /**
     * Creates a multi-resolution icon suitable for taskbar and titlebar.
     */
    public static Image[] createMultiResolutionIcons() {
        return new Image[]{
                createPigTailImage(16),
                createPigTailImage(32),
                createPigTailImage(48),
                createPigTailImage(64),
                createPigTailImage(128),
                createPigTailImage(256)
        };
    }
}
