package de.tobiashh.javafx;

import de.tobiashh.javafx.properties.Properties;

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

public class ImageTools {
    /**
     * In dieser Methode wird ein Bild geladen.
     *
     * @param imageFile
     * @return
     * @throws IOException
     */
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

            System.out.println("imageWidth: " + imageWidth);
            System.out.println("imageHeight: " + imageHeight);

            int factorWidth = imageWidth / Properties.getInstance().getTileSize();
            int factorHeight = imageHeight / Properties.getInstance().getTileSize();

            ImageReadParam param = new ImageReadParam();
            int factor = (factorWidth > factorHeight) ? factorHeight : factorWidth;

            if(factor < 1) {
                System.out.println("image " + imageFile.getName() + " smaller than tileDim");
                factor = 1;
            }

            System.out.println("factorWidth: " + factorWidth);
            System.out.println("factorHeight: " + factorHeight);
            System.out.println("factor: " + factor);

            if(factor <= 0) System.exit(-1);

            param.setSourceSubsampling(factor, factor, 0, 0);

            Rectangle sourceRegion = new Rectangle();
            boolean orientationLandscape = (imageWidth > imageHeight) ? true : false;

            System.out.println("orientationLandscape: " + orientationLandscape);
            System.out.println("width: " + imageWidth);
            System.out.println("height: " +imageHeight);

            if(orientationLandscape){
                sourceRegion.setSize(imageHeight, imageHeight);
                sourceRegion.setLocation((imageWidth - imageHeight) / 2, 0);
            } else {
                sourceRegion.setSize(imageWidth, imageWidth);
                sourceRegion.setLocation(0, (imageHeight - imageWidth) / 2);
            }

            System.out.println(sourceRegion.toString());

            param.setSourceRegion(sourceRegion);

            retval = reader.read(0, param);
        } else {
            System.out.println("no compatible reader");
        }

        return retval;
    }


    public static BufferedImage calculateScaledImage(BufferedImage bImage, int width, int height, boolean highQuality) {
        if (bImage.getWidth() == width && bImage.getHeight() == height) return bImage;

        BufferedImage retval = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = (Graphics2D) retval.getGraphics();
        g2d.addRenderingHints((highQuality) ? getHQRenderingHints() : getLQRenderingHints());
        g2d.drawImage(bImage, 0, 0, width, height, null);

        return retval;
    }

    public static RenderingHints getHQRenderingHints() {
        Map<RenderingHints.Key, Object> map = new HashMap<>();

        map.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        map.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        map.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        map.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);

        return new RenderingHints(map);
    }

    public static RenderingHints getLQRenderingHints() {
        Map<RenderingHints.Key, Object> map = new HashMap<>();

        map.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        map.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        map.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        map.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);

        return new RenderingHints(map);
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
