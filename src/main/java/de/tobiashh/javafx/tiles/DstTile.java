package de.tobiashh.javafx.tiles;

import de.tobiashh.javafx.compareable.SimpleSquareComparableImage;

import java.awt.image.BufferedImage;
import java.util.logging.Logger;

public class DstTile extends SimpleSquareComparableImage {
    private final static Logger LOGGER = Logger.getLogger(DstTile.class.getName());

    private final BufferedImage srcImage;
    // Actual only used for DstTileInformation
    private final String filename;

    public DstTile(BufferedImage image, String filename, int compareSize)
    {
        LOGGER.info("DstTile.DstTile with compareSize " + compareSize);
        setDataImage(image, compareSize);
        this.srcImage = image;
        this.filename = filename;
    }

    public BufferedImage getImage() {
        LOGGER.info("DstTile.getImage");
        return srcImage;
    }

    public String getFilename(){
        LOGGER.info("DstTile.getFilename");
        return filename;}
}
