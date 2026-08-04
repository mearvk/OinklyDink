package com.oinklydink.launcher;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

/**
 * Exports the pig tail icon as a Windows .ico file containing multiple resolutions.
 *
 * ICO format:
 *   - ICONDIR header (6 bytes)
 *   - ICONDIRENTRY per image (16 bytes each)
 *   - Raw PNG data for each image
 *
 * Modern .ico files can embed PNG-compressed images (supported since Windows Vista).
 * This produces a multi-resolution .ico suitable for desktop shortcuts, taskbar,
 * and Explorer display.
 *
 * Run: java -cp oinklydink-launcher-1.0.0.jar com.oinklydink.launcher.IcoExporter [output-dir]
 */
public class IcoExporter {

    /** Sizes to embed in the .ico (covers all Windows use cases). */
    private static final int[] ICO_SIZES = {16, 24, 32, 48, 64, 128, 256};

    public static void main(String[] args) throws Exception {
        String outputDir = args.length > 0 ? args[0] : ".";

        File dir = new File(outputDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // Generate the .ico file
        File icoFile = new File(dir, "oinklydink.ico");
        writeIco(icoFile);
        System.out.println("Exported: " + icoFile.getAbsolutePath());

        // Also export individual PNGs (same as IconExporter, for completeness)
        int[] pngSizes = {16, 24, 32, 48, 64, 128, 256, 512};
        for (int size : pngSizes) {
            BufferedImage img = PigTailIcon.createPigTailImage(size);
            File outFile = new File(dir, "oinklydink-" + size + ".png");
            ImageIO.write(img, "PNG", outFile);
            System.out.println("Exported: " + outFile.getAbsolutePath());
        }
    }

    /**
     * Writes a multi-resolution .ico file with PNG-compressed images.
     */
    public static void writeIco(File output) throws IOException {
        // Render all sizes as PNG byte arrays
        byte[][] pngData = new byte[ICO_SIZES.length][];
        for (int i = 0; i < ICO_SIZES.length; i++) {
            BufferedImage img = PigTailIcon.createPigTailImage(ICO_SIZES[i]);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(img, "PNG", baos);
            pngData[i] = baos.toByteArray();
        }

        try (FileOutputStream fos = new FileOutputStream(output);
             DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(fos))) {

            int imageCount = ICO_SIZES.length;

            // ICONDIR header (6 bytes)
            writeShortLE(dos, 0);      // Reserved, must be 0
            writeShortLE(dos, 1);      // Type: 1 = ICO
            writeShortLE(dos, imageCount);  // Number of images

            // Calculate data offset: header (6) + entries (imageCount * 16)
            int dataOffset = 6 + imageCount * 16;

            // ICONDIRENTRY for each image (16 bytes each)
            for (int i = 0; i < imageCount; i++) {
                int size = ICO_SIZES[i];
                int width = size >= 256 ? 0 : size;   // 0 means 256 in ICO format
                int height = size >= 256 ? 0 : size;

                dos.writeByte(width);       // Width (0 = 256)
                dos.writeByte(height);      // Height (0 = 256)
                dos.writeByte(0);           // Color palette count (0 = no palette)
                dos.writeByte(0);           // Reserved
                writeShortLE(dos, 1);       // Color planes
                writeShortLE(dos, 32);      // Bits per pixel
                writeIntLE(dos, pngData[i].length);  // Image data size
                writeIntLE(dos, dataOffset);         // Offset to image data

                dataOffset += pngData[i].length;
            }

            // Image data (PNG bytes)
            for (byte[] data : pngData) {
                dos.write(data);
            }
        }
    }

    /** Writes a 16-bit value in little-endian order. */
    private static void writeShortLE(DataOutputStream dos, int value) throws IOException {
        dos.writeByte(value & 0xFF);
        dos.writeByte((value >> 8) & 0xFF);
    }

    /** Writes a 32-bit value in little-endian order. */
    private static void writeIntLE(DataOutputStream dos, int value) throws IOException {
        dos.writeByte(value & 0xFF);
        dos.writeByte((value >> 8) & 0xFF);
        dos.writeByte((value >> 16) & 0xFF);
        dos.writeByte((value >> 24) & 0xFF);
    }
}
