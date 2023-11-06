package de.tobiashh.javafx.tools;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImageTools {
    private final static Logger LOGGER = LoggerFactory.getLogger(ImageTools.class.getName());

    public static BufferedImage loadTileImage(Path imageFile, Path cachePath, int tileSize, boolean highQuality) {
        BufferedImage cachedImage = readCacheImage(imageFile, cachePath, tileSize);
        if (cachedImage != null) return cachedImage;

        BufferedImage image = readImage(imageFile, tileSize, highQuality);
        writeCacheImage(image, imageFile, cachePath);

        return image;
    }

    private static void writeCacheImage(BufferedImage image, Path imageFile, Path cachePath) {
        try {
            if (image != null) {
                Path cacheFile = getCacheFile(imageFile, cachePath, image.getWidth());
                LOGGER.info("writeCacheImage {} for {}", cacheFile.toString(), imageFile.toString());
                ImageIO.write(image, "png", cacheFile.toFile());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static BufferedImage readImage(Path imageFile, int tileSize, boolean highQuality) {
        LOGGER.info("readImage {} with tileSize {}", imageFile, tileSize);
        try {
            ImageInputStream iis = ImageIO.createImageInputStream(imageFile.toFile());
            Iterator<ImageReader> iterator = ImageIO.getImageReaders(iis);

            if (iterator.hasNext()) {
                ImageReader reader = iterator.next();
                reader.setInput(iis, true, true);
                ImageReadParam param = getImageReadParam(reader.getWidth(0), reader.getHeight(0), tileSize);
                return removeAlpha(ImageTools.calculateScaledImage(reader.read(0, param), tileSize, tileSize, highQuality));
            } else {
                LOGGER.warn("no compatible reader for {}", imageFile);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static ImageReadParam getImageReadParam(int imageWidth, int imageHeight, int tileSize) {
        ImageReadParam param = new ImageReadParam();

        int factorWidth = imageWidth / tileSize;
        int factorHeight = imageHeight / tileSize;
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
        return param;
    }

    private static BufferedImage removeAlpha(BufferedImage image) {
        if (image.getType() == BufferedImage.TYPE_INT_RGB) return image;

        BufferedImage noAlphaImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = (Graphics2D) noAlphaImage.getGraphics();
        g2d.addRenderingHints(getHighQualityRenderingHints());
        g2d.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), null);
        return noAlphaImage;
    }

    private static BufferedImage readCacheImage(Path imageFile, Path cachePath, int tileSize) {
        try {
            Path cacheFile = getCacheFile(imageFile, cachePath, tileSize);
            if (Files.exists(cacheFile) && Files.isRegularFile(cacheFile)) {
                LOGGER.info("readCacheImage {} with tileSize {}", imageFile, tileSize);
                return ImageIO.read(cacheFile.toFile());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Path getCacheFile(Path imageFile, Path cachePath, int tileSize) {
        return cachePath.resolve(imageFile
                .getFileName()
                .toString()
                .substring(0, imageFile.getFileName().toString().lastIndexOf('.'))
                .concat("_" + Math.abs(imageFile.getParent().hashCode()))
                .concat("_" + tileSize)
                .concat("_date" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")))
                .concat(".png")
        );
    }

    public static BufferedImage colorAlignment(BufferedImage mosaic,
                                               BufferedImage original, int percent) {
        LOGGER.debug("colorAlignment {}%", percent);
        if (percent == 0)
            return mosaic;
        if (mosaic.getWidth() != original.getWidth()
                || mosaic.getHeight() != original.getHeight())
            return mosaic;

        int mosaicMeanColor = getMeanColor(mosaic);
        int originalMeanColor = getMeanColor(original);

        return getAdjustedImage(
                (red(mosaicMeanColor) - red(originalMeanColor)) * percent / 100,
                (green(mosaicMeanColor) - green(originalMeanColor)) * percent / 100,
                (blue(mosaicMeanColor) - blue(originalMeanColor)) * percent / 100,
                mosaic
        );
    }

    private static BufferedImage getAdjustedImage(int redCorrection, int greenCorrection, int blueCorrection, BufferedImage image) {
        int rgb;
        int width = image.getWidth();
        int height = image.getHeight();

        BufferedImage returnValue = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                rgb = image.getRGB(x, y);

                returnValue.setRGB(x, y, new Color(
                        clampColor(red(rgb) - redCorrection),
                        clampColor(green(rgb) - greenCorrection),
                        clampColor(blue(rgb) - blueCorrection)
                ).getRGB());
            }
        }
        return returnValue;
    }

    private static int getMeanColor(BufferedImage image) {
        long redSum = 0;
        long greenSum = 0;
        long blueSum = 0;

        int height = image.getHeight();
        int width = image.getWidth();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);

                redSum += red(rgb);
                greenSum += green(rgb);
                blueSum += blue(rgb);
            }
        }

        int size = width * height;

        return rgb((int) (redSum / size), (int) (greenSum / size), (int) (blueSum / size));
    }

    private static int clampColor(int value) {
        return Math.max(0, Math.min(255, value));
    }

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

    public static int rgb(int red, int green, int blue) {
        return red << 16 | green << 8 | blue;
    }

    public static int red(int rgb) {
        return (rgb >> 16) & 0x000000FF;
    }

    public static int green(int rgb) {
        return (rgb >> 8) & 0x000000FF;
    }

    public static int blue(int rgb) {
        return rgb & 0x000000FF;
    }

}
