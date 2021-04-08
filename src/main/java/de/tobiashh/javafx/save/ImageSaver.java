package de.tobiashh.javafx.save;

import de.tobiashh.javafx.tiles.OriginalTile;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.logging.Logger;

public class ImageSaver implements Runnable {
	private final static Logger LOGGER = Logger.getLogger(ImageSaver.class.getName());
	
	private final Path file;
	private final OriginalTile[] tiles;
	private final int tileSize;
	private final int tilesX;
	private final int tilesY;

	public ImageSaver(Path file, OriginalTile[] tiles, int tilesX, int tilesY, int tileSize) {
		this.tiles = tiles;
		this.tileSize = tileSize;
		this.tilesX = tilesX;
		this.tilesY = tilesY;
		this.file = file;
	}
	
	@Override
	public void run() {
		LOGGER.info("Save Image: " + file.getFileName());
		Runtime.getRuntime().gc();
		
		try
		{
			
			Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("png");
			if (writers.hasNext())
			{
				
				ImageWriter writer = writers.next();
				
				ImageOutputStream ios = ImageIO.createImageOutputStream(file.toFile());
				writer.setOutput(ios);
				
				LOGGER.warning("write image to file");
				
				TileRenderImage tm = new TileRenderImage(tilesX, tilesY, tileSize,tiles);
				writer.write(tm);
				writer.dispose();
				ios.close();
			}
			
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		LOGGER.info("Image saved");
	}
	
}
