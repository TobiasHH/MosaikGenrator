package de.tobiashh.javafx;

import de.tobiashh.javafx.tiles.MosaicTile;
import de.tobiashh.javafx.tools.ImageTools;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.Callable;

public class MosaicTileLoadTask implements Callable<Optional<MosaicTile>> {
	private final Path path;
	private final int tileSize;

	public MosaicTileLoadTask(Path mosaicTilesPath, int tileSize) {
		this.path = mosaicTilesPath;
		this.tileSize = tileSize;
	}

	@Override
	public Optional<MosaicTile> call() {
		MosaicTile tile = null;
		try {
			BufferedImage image = ImageTools.loadTileImage(path.toFile());
			if (image != null) {
				tile = new MosaicTile(ImageTools.calculateScaledImage(image, tileSize, tileSize, false), path.getFileName().toString());
			}
		}catch (Exception e)
		{
			e.printStackTrace();
		}
		return Optional.ofNullable(tile);
	}
}
