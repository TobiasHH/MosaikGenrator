package de.tobiashh.javafx.model;

import de.tobiashh.javafx.tools.ImageTools;
import de.tobiashh.javafx.tools.Index2D;
import de.tobiashh.javafx.tools.IndexConverter;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ImageTiler {
    private final int tileSize;
    private final int tilesPerRow;
    private final int tilesPerColumn;
    private final BufferedImage srcImage;
    private final IndexConverter indexConverter;

    ImageTiler(BufferedImage srcImage, int tileSize, int tilesPerRow, int tilesPerColumn){
        if(tileSize <= 0) throw new IllegalArgumentException("tileSize ist kleiner oder gleich 0");
        if(tilesPerRow <= 0) throw new IllegalArgumentException("tilesPerRow ist kleiner oder gleich 0");
        if(tilesPerColumn <= 0) throw new IllegalArgumentException("tilesPerColumn ist kleiner oder gleich 0");
        if(srcImage == null) throw new NullPointerException("srcImage ist null");

        this.tileSize = tileSize;
        this.tilesPerRow = tilesPerRow;
        this.tilesPerColumn = tilesPerColumn;
        this.srcImage = srcImage;
        this.indexConverter = new IndexConverter(tilesPerRow);
    }

    public List<BufferedImage> getTiles(){
        BufferedImage scaledSrcImage = calculateScaledImage(srcImage);
        return getIndexStream()
                .mapToObj(index -> getTileImageForIndex(scaledSrcImage, index))
                .collect(Collectors.toList());
    }

    private IntStream getIndexStream() {
        return IntStream.range(0, tilesPerRow * tilesPerColumn);
    }

    private BufferedImage getTileImageForIndex(BufferedImage scaledSrcImage, int index) {
        return getTileImageForIndex2D(scaledSrcImage, indexConverter.convertLinearTo2D(index));
    }

    private BufferedImage getTileImageForIndex2D(BufferedImage scaledSrcImage, Index2D index2D) {
        return scaledSrcImage.getSubimage(index2D.getX() * tileSize, index2D.getY() * tileSize, tileSize, tileSize);
    }

    private BufferedImage calculateScaledImage(BufferedImage srcImage) {
        return ImageTools.calculateScaledImage(srcImage, getScaledWidth(), getScaledHeight(), true);
    }

    private int getScaledWidth() {
        return tilesPerRow * tileSize;
    }

    private int getScaledHeight() {
        return tilesPerColumn * tileSize;
    }
}
