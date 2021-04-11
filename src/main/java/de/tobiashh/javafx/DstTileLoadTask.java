package de.tobiashh.javafx;

import de.tobiashh.javafx.tiles.DstTile;
import de.tobiashh.javafx.tools.ImageTools;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

public class DstTileLoadTask implements Callable<Optional<DstTile>> {
	private final static Logger LOGGER = Logger.getLogger(DstTileLoadTask.class.getName());
	private final Path dstTilesPath;
	private final int tileSize;
	private final int compareSize;

	public DstTileLoadTask(Path dstTilesPath, int tileSize, int compareSize) {
		LOGGER.info("DstTileLoadTask.DstTileLoadTask");
		this.dstTilesPath = dstTilesPath;
		this.tileSize = tileSize;
		this.compareSize = compareSize;
	}

	@Override
	public Optional<DstTile> call() {
		LOGGER.info("Load Tile: " + dstTilesPath.getFileName());
		DstTile tile = null;
		try {
			BufferedImage image = ImageTools.loadTileImage(dstTilesPath.toFile(), tileSize);
			if (image != null) {
				tile = new DstTile(ImageTools.calculateScaledImage(image, tileSize, tileSize, false), dstTilesPath.getFileName().toString(), compareSize);
			}
		}catch (Exception e)
		{
			e.printStackTrace();
		}

		LOGGER.info("Tile loaded");
		return Optional.ofNullable(tile);
	}
}
