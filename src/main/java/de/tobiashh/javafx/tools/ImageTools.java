package de.tobiashh.javafx.tools;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImageTools {
    private final static Logger LOGGER = LoggerFactory.getLogger(ImageTools.class.getName());

    public static BufferedImage loadTileImage(File imageFile, int tileSize)
            throws IOException {
        LOGGER.debug("loadTileImage {} with tileSize {}", imageFile, tileSize);
        BufferedImage returnValue = null;

        ImageInputStream iis = ImageIO.createImageInputStream(imageFile);
        Iterator<ImageReader> iterator = ImageIO.getImageReaders(iis);

        if (iterator.hasNext()) {
            ImageReader reader = iterator.next();
            reader.setInput(iis, true, true);
            int imageWidth = reader.getWidth(0);
            int imageHeight = reader.getHeight(0);

            int factorWidth = imageWidth / tileSize;
            int factorHeight = imageHeight / tileSize;

            ImageReadParam param = new ImageReadParam();
            int factor = Math.min(factorWidth, factorHeight);

            if (factor < 1) {
                factor = 1;
            }

            param.setSourceSubsampling(factor, factor, 0, 0);

            Rectangle sourceRegion = new Rectangle();
            boolean orientationLandscape = imageWidth > imageHeight;

            if (orientationLandscape) {
                //noinspection SuspiciousNameCombination
                sourceRegion.setSize(imageHeight, imageHeight);
                sourceRegion.setLocation((imageWidth - imageHeight) / 2, 0);
            } else {
                //noinspection SuspiciousNameCombination
                sourceRegion.setSize(imageWidth, imageWidth);
                sourceRegion.setLocation(0, (imageHeight - imageWidth) / 2);
            }

            param.setSourceRegion(sourceRegion);

            returnValue = reader.read(0, param);

             if (returnValue.getType() != BufferedImage.TYPE_INT_RGB)
           {
                BufferedImage noAlphaImage = new BufferedImage(returnValue.getWidth(), returnValue.getHeight(), BufferedImage.TYPE_INT_RGB);
                Graphics2D g2d = (Graphics2D) noAlphaImage.getGraphics();
                g2d.addRenderingHints(getHighQualityRenderingHints());
                g2d.drawImage(returnValue, 0, 0, returnValue.getWidth(), returnValue.getHeight(), null);
                returnValue = noAlphaImage;
            }
        } else {
            // TODO Kommen hier die gleichen Dateien raus wie bei ImageIO ?
//            no compatible reader for: C:\Users\luech\Dropbox\Docs and Images\Interessante Bilder Dropbox\XXX\2D\f93d67d40c3de303deb1ccd3b1e0b98356303b34.jpg
//            no compatible reader for: C:\Users\luech\Dropbox\Docs and Images\Interessante Bilder Dropbox\XXX\2D\playful-promises-corsets-waspies-playful-promises-anneliese-black-lace-curve-waspie-15779894624304_1024x1024_3eceabba-5bd7-4b83-beb8-3aea0672fcd0.jpg

            LOGGER.warn("no compatible reader for {}", imageFile);
        }
        return returnValue;
    }

    public static BufferedImage colorAlignment(BufferedImage mosaic,
                                               BufferedImage original, int percent) {
        LOGGER.debug("colorAlignment {}%", percent);
        if (percent == 0)
            return mosaic;
        if (mosaic.getWidth() != original.getWidth()
                || mosaic.getHeight() != original.getHeight())
            return mosaic;

        int width = mosaic.getWidth();
        int height = mosaic.getHeight();

        BufferedImage returnValue = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        long sr1 = 0;
        long sg1 = 0;
        long sb1 = 0;
        long sr2 = 0;
        long sg2 = 0;
        long sb2 = 0;

        int rgb1;
        int rgb2;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                rgb1 = mosaic.getRGB(x, y);
                rgb2 = original.getRGB(x, y);

                sr1 += red(rgb1);
                sg1 += green(rgb1);
                sb1 += blue(rgb1);
                sr2 += red(rgb2);
                sg2 += green(rgb2);
                sb2 += blue(rgb2);
            }
        }

        int size = width * height;

        sr1 /= size;
        sg1 /= size;
        sb1 /= size;
        sr2 /= size;
        sg2 /= size;
        sb2 /= size;

        int red;
        int green;
        int blue;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                rgb1 = mosaic.getRGB(x, y);

                red = (int) clamp(red(rgb1) - (sr1 - sr2) * percent / 100, 0, 255);
                green = (int) clamp(green(rgb1) - (sg1 - sg2) * percent / 100, 0, 255);
                blue = (int) clamp(blue(rgb1) - (sb1 - sb2) * percent / 100, 0, 255);

                returnValue.setRGB(x, y, new Color(red, green, blue).getRGB());
            }
        }

        return returnValue;
    }

    private static long clamp(long value, long min, long max) { return Math.max(min, Math.min(max, value)); }

    public static BufferedImage opacityAdaption(BufferedImage mosaic,
                                                BufferedImage original, int percent) {
        LOGGER.debug("opacityAdaption {}%", percent);
        if (percent == 100)
            return mosaic;
        if (percent == 0)
            return original;

        if (mosaic.getWidth() != original.getWidth()
                || mosaic.getHeight() != original.getHeight())
            return mosaic;

        int width = mosaic.getWidth();
        int height = mosaic.getHeight();

        BufferedImage returnValue = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_RGB);

        int rgb1;
        int rgb2;

        int red;
        int green;
        int blue;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                rgb1 = mosaic.getRGB(x, y);
                rgb2 = original.getRGB(x, y);

                red = red(rgb2) + (red(rgb1) - red(rgb2)) * percent / 100;
                green = green(rgb2) + (green(rgb1) - green(rgb2)) * percent / 100;
                blue = blue(rgb2) + (blue(rgb1) - blue(rgb2)) * percent / 100;

                returnValue.setRGB(x, y, new Color(red, green, blue).getRGB());
            }
        }
        return returnValue;
    }

    public static BufferedImage calculateScaledImage(BufferedImage bImage, int width, int height, boolean highQuality) {
        LOGGER.debug("calculateScaledImage with {},{}", width, height);
        if (bImage.getWidth() == width && bImage.getHeight() == height) return bImage;

        BufferedImage returnValue = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = (Graphics2D) returnValue.getGraphics();
        g2d.addRenderingHints((highQuality) ? getHighQualityRenderingHints() : getLowQualityRenderingHints());
        g2d.drawImage(bImage, 0, 0, width, height, null);

        return returnValue;
    }

    public static RenderingHints getHighQualityRenderingHints() {
        Map<RenderingHints.Key, Object> map = new HashMap<>();

        map.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        map.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        map.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        map.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);

        return new RenderingHints(map);
    }

    public static RenderingHints getLowQualityRenderingHints() {
        Map<RenderingHints.Key, Object> map = new HashMap<>();

        map.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        map.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        map.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        map.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);

        return new RenderingHints(map);
    }

    public static int red(int rgb) { return (rgb >> 16) & 0x000000FF; }

    public static int green(int rgb) {
        return (rgb >> 8) & 0x000000FF;
    }

    public static int blue(int rgb) {
        return rgb & 0x000000FF;
    }

}
