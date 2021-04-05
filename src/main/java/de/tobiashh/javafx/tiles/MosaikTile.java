package de.tobiashh.javafx.tiles;

import de.tobiashh.javafx.compareable.SimpleSquareCoparableImage;

import java.awt.image.BufferedImage;
import java.nio.file.Path;

public class MosaikTile extends SimpleSquareCoparableImage {
    BufferedImage srcImage;
    // Actual only used for MosaikTileInformation
    String filename;

    public MosaikTile(BufferedImage image, String filename)
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
