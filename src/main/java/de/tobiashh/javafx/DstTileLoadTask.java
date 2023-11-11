package de.tobiashh.javafx;

import de.tobiashh.javafx.tiles.DstTile;
import de.tobiashh.javafx.tools.ImageTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.Callable;

public class DstTileLoadTask implements Callable<Optional<DstTile>> {
    private final static Logger LOGGER = LoggerFactory.getLogger(DstTileLoadTask.class.getName());
    private final Path dstTilesPath;
    private final Path cachePath;
    private final int tileSize;
    private final int compareSize;

    public DstTileLoadTask(Path dstTilesPath, Path cachePath, int tileSize, int compareSize) {
        LOGGER.debug("DstTileLoadTask");
        this.dstTilesPath = dstTilesPath;
        this.cachePath = cachePath;
        this.tileSize = tileSize;
        this.compareSize = compareSize;
    }

    @Override
    public Optional<DstTile> call() {
        LOGGER.debug("Load Tile: " + dstTilesPath.getFileName());
        DstTile tile = null;

        BufferedImage image = ImageTools.loadTileImage(dstTilesPath, cachePath, compareSize, true);
        if (image != null) {
            tile = new DstTile(image, dstTilesPath, cachePath, tileSize, compareSize);
        }

        LOGGER.debug("Tile loaded");
        return Optional.ofNullable(tile);
    }
}
