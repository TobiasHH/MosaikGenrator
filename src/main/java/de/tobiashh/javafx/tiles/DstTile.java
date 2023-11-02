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
    private final int tileSize;
    private SoftReference<BufferedImage> srcImage;

    public DstTile(BufferedImage image, Path file, int tileSize, int compareSize) {
        LOGGER.debug("DstTile {} with tileSize {} and compareSize {}", file, tileSize, compareSize);
        setDataImage(image, compareSize);
        this.file = file;
        this.tileSize = tileSize;
    }

    public BufferedImage getImage() {
        BufferedImage retval = srcImage != null ? srcImage.get() : null;
        if (retval == null) {
            try {
                BufferedImage image = ImageTools.loadTileImage(file.toFile(), tileSize, true);
                srcImage = new SoftReference<>(image);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return srcImage.get();
    }

    public String getFilename() {
        LOGGER.debug("getFilename");
        return file.getFileName().toString();
    }
}
