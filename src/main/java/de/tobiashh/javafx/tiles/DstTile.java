package de.tobiashh.javafx.tiles;

import de.tobiashh.javafx.compareable.SimpleSquareComparableImage;

import java.awt.image.BufferedImage;

public class DstTile extends SimpleSquareComparableImage {
    BufferedImage srcImage;
    // Actual only used for DstTileInformation
    String filename;

    public DstTile(BufferedImage image, String filename)
    {
        setDataImage(image);
        this.srcImage = image;
        this.filename = filename;
    }

    public BufferedImage getImage() {
        return srcImage;
    }

    public String getFilename(){ return filename;}
}
