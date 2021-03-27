package de.tobiashh.javafx.tiles;

import de.tobiashh.javafx.compareable.SimpleSquareCoparableImage;

import java.awt.image.BufferedImage;

public class MosaikTile extends SimpleSquareCoparableImage {
    BufferedImage srcImage;

    public MosaikTile(BufferedImage image)
    {
        setDataImage(image);
        this.srcImage = image;
    }

    public BufferedImage getImage() {
        return srcImage;
    }
}
