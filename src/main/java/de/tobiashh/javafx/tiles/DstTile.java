package de.tobiashh.javafx.tiles;

import de.tobiashh.javafx.compareable.SimpleSquareComparableImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;

public class DstTile extends SimpleSquareComparableImage {
    private final static Logger LOGGER = LoggerFactory.getLogger(DstTile.class.getName());

    private final BufferedImage srcImage;
    // Actual only used for DstTileInformation
    private final String filename;

    public DstTile(BufferedImage image, String filename, int compareSize)
    {
        LOGGER.debug("DstTile {} with compareSize {}", filename, compareSize);
        setDataImage(image, compareSize);
        this.srcImage = image;
        this.filename = filename;
    }

    public BufferedImage getImage() {
        LOGGER.debug("getImage");
        return srcImage;
    }

    public String getFilename(){
        LOGGER.debug("getFilename");
        return filename;}
}
