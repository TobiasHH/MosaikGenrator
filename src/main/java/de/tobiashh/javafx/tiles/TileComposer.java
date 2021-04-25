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

    private BufferedImage deepCopy(BufferedImage bi) {
        ColorModel cm = bi.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = bi.copyData(bi.getRaster().createCompatibleWritableRaster());
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }

    public BufferedImage compose(BufferedImage originalImage, BufferedImage mosaikImage)
    {
        BufferedImage returnValue = deepCopy(mosaikImage);

        if(originalImage.getWidth() == mosaikImage.getWidth() && originalImage.getHeight() == mosaikImage.getHeight()) {
            if (postColorAlignment > 0) {
                returnValue = ImageTools.colorAlignment(returnValue, originalImage, postColorAlignment);
            }

            if (opacity < 100) {
                returnValue = ImageTools.opacityAdaption(returnValue, originalImage, opacity);
            }
        }

        return returnValue;
    }
}
