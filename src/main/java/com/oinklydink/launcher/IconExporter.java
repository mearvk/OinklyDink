package com.oinklydink.launcher;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Utility to export the pig tail icon as PNG files for desktop integration.
 * Run: java -cp oinklydink-launcher-1.0.0.jar com.oinklydink.launcher.IconExporter [output-dir]
 */
public class IconExporter {

    public static void main(String[] args) throws Exception {
        String outputDir = args.length > 0 ? args[0] : ".";

        int[] sizes = {16, 24, 32, 48, 64, 128, 256, 512};

        for (int size : sizes) {
            BufferedImage img = PigTailIcon.createPigTailImage(size);
            File outFile = new File(outputDir, "oinklydink-" + size + ".png");
            ImageIO.write(img, "PNG", outFile);
            System.out.println("Exported: " + outFile.getAbsolutePath());
        }

        // Also export a default one at 256px as the main icon
        BufferedImage main = PigTailIcon.createPigTailImage(256);
        File mainFile = new File(outputDir, "oinklydink.png");
        ImageIO.write(main, "PNG", mainFile);
        System.out.println("Exported: " + mainFile.getAbsolutePath());
    }
}
