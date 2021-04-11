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

	public DstTileLoadTask(Path dstTilesPath, int tileSize) {
		this.dstTilesPath = dstTilesPath;
		this.tileSize = tileSize;
	}

	@Override
	public Optional<DstTile> call() {
		DstTile tile = null;
		try {
			BufferedImage image = ImageTools.loadTileImage(dstTilesPath.toFile());
			if (image != null) {
				tile = new DstTile(ImageTools.calculateScaledImage(image, tileSize, tileSize, false), dstTilesPath.getFileName().toString());
			}
		}catch (Exception e)
		{
			e.printStackTrace();
		}
		return Optional.ofNullable(tile);
	}
}
