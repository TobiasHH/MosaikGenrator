package de.tobiashh.javafx.save;

import de.tobiashh.javafx.tiles.OriginalTile;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Logger;

public class ImageSaver implements Runnable {
	private final static Logger LOGGER = Logger.getLogger(ImageSaver.class.getName());
	
	private File file;
	private OriginalTile[] tiles;
	private final int tileSize;
	private final int tilesX;
	private final int tilesY;

	public ImageSaver(File file, OriginalTile[] tiles, int tilesX, int tilesY, int tileSize) {
		this.tiles = tiles;
		this.tileSize = tileSize;
		this.tilesX = tilesX;
		this.tilesY = tilesY;
		this.file = file;
	}
	
	@Override
	public void run() {
		LOGGER.info("Save Image: " + file.getName());
		Runtime.getRuntime().gc();
		
		try
		{
			
			Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("png");
			if (writers.hasNext())
			{
				
				ImageWriter writer = writers.next();
				
				ImageOutputStream ios = ImageIO.createImageOutputStream(file);
				writer.setOutput(ios);
				
				LOGGER.warning("write image to file");
				
				TileImage tm = new TileImage(tilesX, tilesY, tileSize,tiles);
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
