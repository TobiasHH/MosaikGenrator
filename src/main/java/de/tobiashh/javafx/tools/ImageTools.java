package de.tobiashh.javafx.tools;

import de.tobiashh.javafx.properties.Properties;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ImageTools {

    public static BufferedImage loadTileImage(File imageFile)
            throws IOException {
        BufferedImage retval = null;

        ImageInputStream iis = ImageIO.createImageInputStream(imageFile);
        Iterator<ImageReader> iter = ImageIO.getImageReaders(iis);

        if (iter.hasNext()) {
            ImageReader reader = iter.next();
            reader.setInput(iis, true, true);
            int imageWidth = reader.getWidth(0);
            int imageHeight = reader.getHeight(0);

            int factorWidth = imageWidth / Properties.getInstance().getTileSize();
            int factorHeight = imageHeight / Properties.getInstance().getTileSize();

            ImageReadParam param = new ImageReadParam();
            int factor = Math.min(factorWidth, factorHeight);

            if(factor < 1) {
                factor = 1;
            }

            param.setSourceSubsampling(factor, factor, 0, 0);

            Rectangle sourceRegion = new Rectangle();
            boolean orientationLandscape = imageWidth > imageHeight;

            if(orientationLandscape){
                sourceRegion.setSize(imageHeight, imageHeight);
                sourceRegion.setLocation((imageWidth - imageHeight) / 2, 0);
            } else {
                sourceRegion.setSize(imageWidth, imageWidth);
                sourceRegion.setLocation(0, (imageHeight - imageWidth) / 2);
            }

            param.setSourceRegion(sourceRegion);

            retval = reader.read(0, param);
        } else {
            System.err.println("no compatible reader");
        }

        return retval;
    }

    public static BufferedImage colorAlignment(BufferedImage mosaik,
                                               BufferedImage original, int prozent) {
        if (prozent == 0)
            return mosaik;
        if (mosaik.getWidth() != original.getWidth()
                || mosaik.getHeight() != original.getHeight())
            return mosaik;

        int width = mosaik.getWidth();
        int height = mosaik.getHeight();

        BufferedImage retval = new BufferedImage(width, height,	BufferedImage.TYPE_INT_RGB);

        System.out.println("width = " + width);
        System.out.println("height = " + height);

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
                rgb1 = mosaik.getRGB(x, y);
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
                rgb1 = mosaik.getRGB(x, y);

                red = (int) (red(rgb1) - (sr1 - sr2) * prozent / 100);
                green = (int) (green(rgb1) - (sg1 - sg2) * prozent / 100);
                blue = (int) (blue(rgb1) - (sb1 - sb2) * prozent / 100);

                if (red < 0)
                    red = 0;
                if (green < 0)
                    green = 0;
                if (blue < 0)
                    blue = 0;
                if (red > 255)
                    red = 255;
                if (green > 255)
                    green = 255;
                if (blue > 255)
                    blue = 255;

                retval.setRGB(x, y, new Color(red, green, blue).getRGB());
            }
        }

        return retval;
    }

    public static BufferedImage opacityAdaption(BufferedImage mosaik,
                                                BufferedImage original, int prozent) {
        if (prozent == 100)
            return mosaik;
        if (prozent == 0)
            return original;

        if (mosaik.getWidth() != original.getWidth()
                || mosaik.getHeight() != original.getHeight())
            return mosaik;

        int width = mosaik.getWidth();
        int height = mosaik.getHeight();

        BufferedImage retval = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_RGB);

        int rgb1;
        int rgb2;

        int red;
        int green;
        int blue;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                rgb1 = mosaik.getRGB(x, y);
                rgb2 = original.getRGB(x, y);

                red = red(rgb2) + (red(rgb1) - red(rgb2)) * prozent / 100;
                green = green(rgb2) + (green(rgb1) - green(rgb2)) * prozent
                        / 100;
                blue = blue(rgb2) + (blue(rgb1) - blue(rgb2)) * prozent / 100;

                retval.setRGB(x, y, new Color(red, green, blue).getRGB());
            }
        }
        return retval;
    }

    public static BufferedImage calculateScaledImage(BufferedImage bImage, int width, int height, boolean highQuality) {
        if (bImage.getWidth() == width && bImage.getHeight() == height) return bImage;

        BufferedImage retval = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = (Graphics2D) retval.getGraphics();
        g2d.addRenderingHints((highQuality) ? getHighQualityRenderingHints() : getLowQualityRenderingHints());
        g2d.drawImage(bImage, 0, 0, width, height, null);

        return retval;
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

    public static BufferedImage blurImage(BufferedImage image) {
        float[] blurKernel = { 0.0f, 1.f / 6.f, 0.0f, 1.f / 6.f, 1.f / 3.f,
                1.f / 6.f, 0.0f, 1.f / 6.f, 0.0f };

        BufferedImageOp op = new ConvolveOp(new Kernel(3, 3, blurKernel),
                ConvolveOp.EDGE_NO_OP, getHighQualityRenderingHints());
        return op.filter(image, null);
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
