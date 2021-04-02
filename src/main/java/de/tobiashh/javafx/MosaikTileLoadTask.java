package de.tobiashh.javafx;

import de.tobiashh.javafx.properties.Properties;
import de.tobiashh.javafx.tiles.MosaikTile;
import de.tobiashh.javafx.tools.ImageTools;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.Callable;

public class MosaikTileLoadTask implements Callable<Optional<MosaikTile>> {
	private final Path path;

	public MosaikTileLoadTask(Path mosaikTilesPath) {
		this.path = mosaikTilesPath;
	}

	@Override
	public Optional<MosaikTile> call() {
		MosaikTile tile = null;
		try {
			BufferedImage image = ImageTools.loadTileImage(path.toFile());
			if (image != null) {
				tile = new MosaikTile(ImageTools.calculateScaledImage(image, Properties.getInstance().getTileSize(), Properties.getInstance().getTileSize(), false));
			}
		}catch (Exception e)
		{
			e.printStackTrace();
		}
		return Optional.ofNullable(tile);
	}
}
