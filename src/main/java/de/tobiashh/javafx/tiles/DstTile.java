package de.tobiashh.javafx.tiles;

import de.tobiashh.javafx.compareable.SimpleSquareComparableImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.lang.ref.SoftReference;

public class DstTile extends SimpleSquareComparableImage {
    private final static Logger LOGGER = LoggerFactory.getLogger(DstTile.class.getName());

    private final SoftReference<BufferedImage> srcImage;
    private final String filename;

    public DstTile(BufferedImage image, String filename, int compareSize)
    {
        LOGGER.debug("DstTile {} with compareSize {}", filename, compareSize);
        setDataImage(image, compareSize);
        this.srcImage = new SoftReference<>(image);
        this.filename = filename;
    }

    public BufferedImage getImage() {
        LOGGER.debug("getImage");
        return srcImage.get();
    }

    public String getFilename(){
        LOGGER.debug("getFilename");
        return filename;}
}
