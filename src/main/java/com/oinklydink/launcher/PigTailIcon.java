package com.oinklydink.launcher;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;

/**
 * Renders a 3D pig's tail icon — soft, rounded, volumetric.
 *
 * The tail is drawn as a thick 3D tube that curls in a spiral,
 * with proper shading (top highlight, bottom shadow), warm pink flesh tones,
 * and a gentle ambient occlusion effect where the curls overlap.
 *
 * The 3D effect is achieved by:
 *  - Drawing the tail as a series of overlapping elliptical segments
 *  - Applying radial gradient fills to simulate cylindrical volume
 *  - Adding specular highlights along the top edge
 *  - Casting soft shadows beneath the curl
 *
 * Careful and comfortable — soft gradients, warm colors, no hard edges.
 */
public class PigTailIcon {

    // Warm pig-flesh pink palette (soft, comforting)
    private static final Color SKIN_LIGHT = new Color(0xFF, 0xC8, 0xD0);
    private static final Color SKIN_MID = new Color(0xF0, 0x9E, 0xAE);
    private static final Color SKIN_DEEP = new Color(0xD4, 0x72, 0x8A);
    private static final Color SKIN_SHADOW = new Color(0xA0, 0x4E, 0x66);
    private static final Color HIGHLIGHT = new Color(0xFF, 0xEC, 0xF0);
    private static final Color SPECULAR = new Color(0xFF, 0xFF, 0xFF, 200);
    private static final Color AMBIENT_SHADOW = new Color(0x60, 0x20, 0x30, 50);
    private static final Color BACKGROUND = new Color(0xFD, 0xF5, 0xF7);

    // Red key colors
    private static final Color KEY_RED = new Color(0xE0, 0x1B, 0x1B);
    private static final Color KEY_RED_BRIGHT = new Color(0xFF, 0x44, 0x44);
    private static final Color KEY_RED_DARK = new Color(0x99, 0x10, 0x10);
    private static final Color KEY_GLOW = new Color(0xFF, 0x33, 0x33, 60);

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

        // Maximum quality rendering
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);

        float scale = size / 64.0f;

        // Soft background
        g2d.setColor(BACKGROUND);
        g2d.fillOval(0, 0, size, size);

        // Subtle circular border
        g2d.setColor(new Color(0xE8, 0xD0, 0xD8));
        g2d.setStroke(new BasicStroke(scale * 0.8f));
        g2d.drawOval(1, 1, size - 3, size - 3);

        // Wiggle: sinusoidal rotation ±12° (comfortable, not jarring)
        double wiggleAngle = Math.sin(2.0 * Math.PI * frame / TOTAL_FRAMES) * 12.0;

        // Draw the 3D pig's tail
        draw3DPigTail(g2d, size, scale, wiggleAngle);

        // Red key overlay
        if (showKey) {
            drawRedKey(g2d, size, scale);
        }

        g2d.dispose();
        return image;
    }

    /**
     * Generates all 44 frames.
     */
    public static BufferedImage[] generateAllFrames(int size, boolean showKey) {
        BufferedImage[] frames = new BufferedImage[TOTAL_FRAMES];
        for (int i = 0; i < TOTAL_FRAMES; i++) {
            frames[i] = createAnimatedFrame(size, i, showKey);
        }
        return frames;
    }

    /**
     * Draws the pig's tail as a 3D tube spiral.
     * Uses segmented rendering — draws the spiral as ~60 overlapping circles
     * with gradient fills to simulate a cylindrical, fleshy curl.
     */
    private static void draw3DPigTail(Graphics2D g2d, int size, float scale, double wiggleDegrees) {
        float cx = size / 2.0f;
        float cy = size / 2.0f;

        AffineTransform original = g2d.getTransform();
        g2d.rotate(Math.toRadians(wiggleDegrees), cx, cy);

        // The tail is parametrized as a spiral: r decreases, angle increases
        // We draw it as a series of filled circles along the path (like a 3D tube)
        int segments = 60;
        float tubeRadius = 4.5f * scale; // thickness of the tail tube

        // Spiral parameters
        // Starts at bottom, curls up and inward in 2.5 revolutions
        float baseRadius = 16 * scale;  // outer radius of the spiral
        float spiralTightening = 0.55f; // how fast it tightens

        // First pass: drop shadow
        for (int i = 0; i < segments; i++) {
            float t = i / (float) segments;
            float[] pos = getSpiralPosition(t, cx, cy, baseRadius, spiralTightening, scale);
            float r = tubeRadius * (1.0f - t * 0.45f); // tapers toward tip

            g2d.setColor(new Color(0, 0, 0, (int) (20 * (1.0f - t * 0.5f))));
            g2d.fill(new Ellipse2D.Float(
                    pos[0] - r + 1.5f * scale,
                    pos[1] - r + 2.0f * scale,
                    r * 2, r * 2));
        }

        // Second pass: main tube body with 3D shading
        for (int i = 0; i < segments; i++) {
            float t = i / (float) segments;
            float[] pos = getSpiralPosition(t, cx, cy, baseRadius, spiralTightening, scale);
            float r = tubeRadius * (1.0f - t * 0.45f);

            // Radial gradient for cylindrical 3D look
            // Light comes from upper-left
            float highlightOffX = -r * 0.3f;
            float highlightOffY = -r * 0.4f;

            RadialGradientPaint tubePaint = new RadialGradientPaint(
                    new Point2D.Float(pos[0] + highlightOffX, pos[1] + highlightOffY),
                    r * 1.4f,
                    new float[]{0.0f, 0.4f, 0.75f, 1.0f},
                    new Color[]{HIGHLIGHT, SKIN_LIGHT, SKIN_MID, SKIN_DEEP}
            );

            g2d.setPaint(tubePaint);
            g2d.fill(new Ellipse2D.Float(pos[0] - r, pos[1] - r, r * 2, r * 2));

            // Outline for definition
            g2d.setColor(new Color(SKIN_SHADOW.getRed(), SKIN_SHADOW.getGreen(),
                    SKIN_SHADOW.getBlue(), (int) (80 + 60 * t)));
            g2d.setStroke(new BasicStroke(scale * 0.4f));
            g2d.draw(new Ellipse2D.Float(pos[0] - r, pos[1] - r, r * 2, r * 2));
        }

        // Third pass: specular highlights along the top
        for (int i = 0; i < segments; i += 2) {
            float t = i / (float) segments;
            float[] pos = getSpiralPosition(t, cx, cy, baseRadius, spiralTightening, scale);
            float r = tubeRadius * (1.0f - t * 0.45f);

            // Small bright dot near the top of each segment
            float specR = r * 0.25f;
            float specX = pos[0] - r * 0.25f;
            float specY = pos[1] - r * 0.45f;

            g2d.setColor(SPECULAR);
            g2d.fill(new Ellipse2D.Float(specX - specR, specY - specR, specR * 2, specR * 1.4f));
        }

        // Base attachment — a small rounded nub at the start
        float[] basePos = getSpiralPosition(0, cx, cy, baseRadius, spiralTightening, scale);
        float baseR = tubeRadius * 1.2f;
        RadialGradientPaint basePaint = new RadialGradientPaint(
                new Point2D.Float(basePos[0] - baseR * 0.2f, basePos[1] - baseR * 0.3f),
                baseR * 1.3f,
                new float[]{0.0f, 0.5f, 1.0f},
                new Color[]{SKIN_LIGHT, SKIN_MID, SKIN_SHADOW}
        );
        g2d.setPaint(basePaint);
        g2d.fill(new Ellipse2D.Float(basePos[0] - baseR, basePos[1] - baseR, baseR * 2, baseR * 2));

        g2d.setTransform(original);
    }

    /**
     * Returns the [x, y] position along the spiral for parameter t (0..1).
     * The spiral starts at the bottom and curls inward clockwise.
     */
    private static float[] getSpiralPosition(float t, float cx, float cy,
                                              float baseRadius, float tightening, float scale) {
        // 2.5 full revolutions
        float angle = (float) (t * 2.5 * 2.0 * Math.PI) - (float) (Math.PI * 0.5);

        // Radius decreases as t increases (spiral gets tighter)
        float r = baseRadius * (1.0f - t * tightening);

        // Add slight vertical offset so it doesn't look perfectly centered
        float offsetY = -2 * scale * t;

        float x = cx + (float) Math.cos(angle) * r;
        float y = cy + (float) Math.sin(angle) * r + offsetY;

        return new float[]{x, y};
    }

    /**
     * Draws a brilliant red key in the upper-right corner.
     */
    private static void drawRedKey(Graphics2D g2d, int size, float scale) {
        float keyX = size * 0.62f;
        float keyY = size * 0.05f;
        float ks = scale * 0.75f;

        float headCx = keyX + 7 * ks;
        float headCy = keyY + 7 * ks;
        float headR = 5.5f * ks;

        // Glow
        g2d.setColor(KEY_GLOW);
        g2d.fill(new Ellipse2D.Float(headCx - headR * 1.8f, headCy - headR * 1.8f,
                headR * 3.6f, headR * 3.6f));

        // Key head - 3D sphere
        RadialGradientPaint keyPaint = new RadialGradientPaint(
                new Point2D.Float(headCx - headR * 0.3f, headCy - headR * 0.3f),
                headR * 1.2f,
                new float[]{0.0f, 0.5f, 1.0f},
                new Color[]{KEY_RED_BRIGHT, KEY_RED, KEY_RED_DARK}
        );
        g2d.setPaint(keyPaint);
        g2d.fill(new Ellipse2D.Float(headCx - headR, headCy - headR, headR * 2, headR * 2));

        // Key hole
        float holeR = 2.0f * ks;
        g2d.setColor(new Color(0x40, 0x08, 0x08));
        g2d.fill(new Ellipse2D.Float(headCx - holeR, headCy - holeR, holeR * 2, holeR * 2));

        // Key shaft
        g2d.setColor(KEY_RED);
        g2d.setStroke(new BasicStroke(2.0f * ks, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        float shaftTop = headCy + headR;
        float shaftBot = shaftTop + 11 * ks;
        g2d.draw(new Line2D.Float(headCx, shaftTop, headCx, shaftBot));

        // Teeth
        g2d.setStroke(new BasicStroke(1.6f * ks, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER));
        g2d.draw(new Line2D.Float(headCx, shaftBot - 4 * ks, headCx + 3.5f * ks, shaftBot - 4 * ks));
        g2d.draw(new Line2D.Float(headCx, shaftBot - 1.5f * ks, headCx + 2.5f * ks, shaftBot - 1.5f * ks));

        // Specular on key head
        g2d.setColor(new Color(0xFF, 0xFF, 0xFF, 180));
        g2d.fill(new Ellipse2D.Float(headCx - headR * 0.5f, headCy - headR * 0.6f,
                headR * 0.6f, headR * 0.4f));
    }

    /**
     * Creates a multi-resolution icon set.
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
