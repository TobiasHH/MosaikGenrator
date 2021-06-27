package de.tobiashh.javafx.save;

import de.tobiashh.javafx.tiles.OriginalTile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.image.*;
import java.util.Vector;

/**
 * 
 * @author tsc
 * 
 */
public class TileRenderImage implements RenderedImage {
	private final static Logger LOGGER = LoggerFactory.getLogger(TileRenderImage.class.getName());

	private final DirectColorModel cm;
	private final SinglePixelPackedSampleModel sm;
	private final int tilesPerRow;
	private final int tilesPerColumn;
	private final int tileSize;
	private final OriginalTile[] tiles;
	
	public TileRenderImage(int tilesPerRow, int tilesPerColumn, int tileSize, OriginalTile[] tiles) {
		LOGGER.info("TileRenderImage");
		this.tilesPerRow = tilesPerRow;
		this.tilesPerColumn = tilesPerColumn;

		this.tileSize = tileSize;
		this.tiles = tiles;

		sm = new SinglePixelPackedSampleModel(DataBuffer.TYPE_INT, tilesPerRow * tileSize, tilesPerColumn * tileSize, new int[] { 0x00FF0000, 0x0000FF00, 0x000000FF });
		cm = new DirectColorModel(24, 0xff0000, 0xff00, 0xff);
	}

	@Override
	public WritableRaster copyData(WritableRaster arg0) {
		return null;
	}

	@Override
	public ColorModel getColorModel() {
		return cm;
	}

	@Override
	public Raster getData() {
		return getData(new Rectangle(0, 0, getWidth(), getHeight()));
	}

	@Override
	public Raster getData(Rectangle rect) {
		LOGGER.debug("getData {}", rect);
		SampleModel nsm = sm.createCompatibleSampleModel(rect.width, rect.height);

		WritableRaster wr = Raster.createWritableRaster(nsm, rect.getLocation());

		int width = rect.width;
		int height = rect.height;
		int startX = rect.x;
		int startY = rect.y;

		int[] data = null;
		Raster raster = null;

		int actualTileX = -1;
		int actualTileY = -1;
		for (int y = startY; y < startY + height; y++)
		{
			for (int x = startX; x < startX + width; x++)
			{
				int tileX = x / tileSize;
				int tileY = y / tileSize;
				if(actualTileX != tileX || actualTileY != tileY) {
					raster = tiles[index(tileX, tileY)].getComposedImage().getRaster();
					actualTileX = tileX;
					actualTileY = tileY;
				}
				if (raster != null) {
					data = raster.getPixel(x % tileSize, y % tileSize, data);
					wr.setPixel(x, y, data);
				}
			}
		}

		return wr;
	}

	private int index(int x, int y)
	{
		return y * tilesPerRow + x;
	}


	@Override
	public int getHeight() {
		return tilesPerColumn * tileSize;
	}

	@Override
	public int getMinTileX() {
		return 0;
	}

	@Override
	public int getMinTileY() {
		return 0;
	}

	@Override
	public int getMinX() {
		return 0;
	}

	@Override
	public int getMinY() {
		return 0;
	}

	@Override
	public int getNumXTiles() {
		return tilesPerRow;
	}

	@Override
	public int getNumYTiles() {
		return tilesPerColumn;
	}

	@Override
	public Object getProperty(String arg0) {
		return null;
	}

	@Override
	public String[] getPropertyNames() {
		return null;
	}

	@Override
	public SampleModel getSampleModel() {
		return sm;
	}

	@Override
	public Vector<RenderedImage> getSources() {
		return null;
	}

	@Override
	public Raster getTile(int arg0, int arg1) {
		return null;
	}

	@Override
	public int getTileGridXOffset() {
		return 0;
	}

	@Override
	public int getTileGridYOffset() {
		return 0;
	}

	@Override
	public int getTileHeight() {
		return tileSize;
	}

	@Override
	public int getTileWidth() {
		return tileSize;
	}

	@Override
	public int getWidth() {
		return tilesPerRow * tileSize;
	}

}
