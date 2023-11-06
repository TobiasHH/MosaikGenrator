package de.tobiashh.javafx.tiles;

import de.tobiashh.javafx.compareable.SimpleSquareComparableImage;
import de.tobiashh.javafx.tools.ImageTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.nio.file.Path;

public class DstTile extends SimpleSquareComparableImage {
    private final static Logger LOGGER = LoggerFactory.getLogger(DstTile.class.getName());
    private final Path file;
    private final Path cachePath;
    private final int tileSize;
    private SoftReference<BufferedImage> cachedSourceImage;

    public DstTile(BufferedImage image, Path file, Path cachePath, int tileSize, int compareSize) {
        LOGGER.debug("DstTile {} with tileSize {} and compareSize {}", file, tileSize, compareSize);
        setDataImage(image, compareSize);
        this.file = file;
        this.cachePath = cachePath;
        this.tileSize = tileSize;
    }

    public BufferedImage getImage() {
        BufferedImage srcImage = cachedSourceImage != null ? cachedSourceImage.get() : null;

        if (srcImage != null) return srcImage;

        cachedSourceImage = new SoftReference<>( ImageTools.loadTileImage(file, cachePath, tileSize, true));
        return cachedSourceImage.get();
    }

    public String getFilename() {
        LOGGER.debug("getFilename");
        return file.getFileName().toString();
    }
}
