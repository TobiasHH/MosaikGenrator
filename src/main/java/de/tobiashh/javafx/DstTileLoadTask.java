package de.tobiashh.javafx;

import de.tobiashh.javafx.tiles.DstTile;
import de.tobiashh.javafx.tools.ImageTools;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.Callable;

public class DstTileLoadTask implements Callable<Optional<DstTile>> {
	private final Path dstTilesPath;
	private final int tileSize;
	private final int compareSize;

	public DstTileLoadTask(Path dstTilesPath, int tileSize, int compareSize) {
		this.dstTilesPath = dstTilesPath;
		this.tileSize = tileSize;
		this.compareSize = compareSize;
	}

	@Override
	public Optional<DstTile> call() {
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
		return Optional.ofNullable(tile);
	}
}
