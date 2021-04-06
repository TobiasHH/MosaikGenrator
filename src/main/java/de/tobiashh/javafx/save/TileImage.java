package de.tobiashh.javafx.save;

import de.tobiashh.javafx.tiles.OriginalTile;

import java.awt.*;
import java.awt.image.*;
import java.util.Vector;

/**
 * 
 * @author tsc
 * 
 */
public class TileImage implements RenderedImage {

	private DirectColorModel cm;
	private SinglePixelPackedSampleModel sm;
	private int tilesX;
	private int tilesY;
	private int tileSize;
	private OriginalTile[] tiles;
//	private boolean[] usedState;
	
	public TileImage(int tilesX, int tilesY, int tileSize, OriginalTile[] tiles) {
		this.tilesX = tilesX;
		this.tilesY = tilesY;

		this.tileSize = tileSize;
		this.tiles = tiles;
		
	//	usedState = new boolean[tiles.length];
		
//		for (int y = 0; y < tilesY; y++)
//		{
//			for (int x = 0; x < tilesX; x++)
//			{
//				usedState[index(x,y)] = false;
//			}
//		}
		
		sm = new SinglePixelPackedSampleModel(DataBuffer.TYPE_INT, tilesX * tileSize, tilesY * tileSize, new int[] { 0x00FF0000, 0x0000FF00, 0x000000FF });
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
		return getData(null);
	}
	
	@Override
	public Raster getData(Rectangle rect) {
		SampleModel nsm = sm.createCompatibleSampleModel(rect.width, rect.height);
		
		WritableRaster wr = Raster.createWritableRaster(nsm, rect.getLocation());
		
		int width = rect.width;
		int height = rect.height;
		int startX = rect.x;
		int startY = rect.y;
		
		int[] tdata = null;
		
		Raster raster;
		OriginalTile tile;
		int tilePosX;
		int tilePosY;
		
		for (int y = startY; y < startY + height; y++)
		{
			for (int x = startX; x < startX + width; x++)
			{
				tilePosX = x / tileSize;
				tilePosY = y / tileSize;
				tile = tiles[index(tilePosX, tilePosY)];
//				usedState[index(tilePosX, tilePosY)] = true;
				raster = tile.getImage().getRaster();
				tdata = raster.getPixel(x % tileSize, y % tileSize, tdata);
				wr.setPixel(x, y, tdata);
			}
		}
		
//		for (int y = 0; y < tilesX; y++)
//		{
//			for (int x = 0; x < tilesY; x++)
//			{
//				if (usedState[index(x,y)] == true)
//				{
//					usedState[index(x,y)] = false;
//				}
//			}
//		}
				
		return wr;
	}

	private int index(int x, int y)
	{
		return y * tilesX + x;
	}


	@Override
	public int getHeight() {
		return tilesY * tileSize;
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
		return 0;
	}

	@Override
	public int getNumYTiles() {
		return 0;
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
		return tilesX * tileSize;
	}

}
