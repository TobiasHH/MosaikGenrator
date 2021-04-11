package de.tobiashh.javafx.tiles;

import de.tobiashh.javafx.compareable.SimpleSquareComparableImage;

import java.awt.image.BufferedImage;

public class DstTile extends SimpleSquareComparableImage {
    private final BufferedImage srcImage;
    // Actual only used for DstTileInformation
    private final String filename;

    public DstTile(BufferedImage image, String filename, int compareSize)
    {
        setDataImage(image, compareSize);
        this.srcImage = image;
        this.filename = filename;
    }

    public BufferedImage getImage() {
        return srcImage;
    }

    public String getFilename(){ return filename;}
}
