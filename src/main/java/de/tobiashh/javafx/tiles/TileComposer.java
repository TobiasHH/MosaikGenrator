package de.tobiashh.javafx.tiles;

import de.tobiashh.javafx.tools.ImageTools;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;

public class TileComposer {
    int opacity;
    int postColorAlignment;

    public TileComposer(int opacity, int postColorAlignment) {
        this.opacity = opacity;
        this.postColorAlignment = postColorAlignment;
    }

    public BufferedImage compose(BufferedImage originalImage, BufferedImage mosaikImage)
    {
        if( originalImage.getWidth() != mosaikImage.getWidth() || originalImage.getHeight() != mosaikImage.getHeight()) return deepCopy(mosaikImage);
        if (opacity == 0) return deepCopy(originalImage);
        return getComposedImage(originalImage, mosaikImage);
    }

    private BufferedImage getComposedImage(BufferedImage originalImage, BufferedImage mosaikImage) {
        return ImageTools.opacityAdaption(ImageTools.colorAlignment(deepCopy(mosaikImage), originalImage, postColorAlignment), originalImage, opacity);
    }

    private BufferedImage deepCopy(BufferedImage bi) {
        ColorModel cm = bi.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = bi.copyData(bi.getRaster().createCompatibleWritableRaster());
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }
}
