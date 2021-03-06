package de.tobiashh.javafx.compareable;

import de.tobiashh.javafx.tools.ImageTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;

public abstract class ComparableImage {
	private final static Logger LOGGER = LoggerFactory.getLogger(ComparableImage.class.getName());

	protected int[] red;
	protected int[] green;
	protected int[] blue;
	
	private int compareSize;

	protected void setDataImage(BufferedImage dataImage, int compareSize) {
		LOGGER.trace("setDataImage");
		this.compareSize = compareSize;
		calculateData(dataImage);
	}

	protected void calculateData(BufferedImage compareImage) {
		LOGGER.trace("compareColor");
		BufferedImage dataImage = compareImage;

		if(compareImage.getWidth() != compareSize || compareImage.getHeight() != compareSize) {
			dataImage = new BufferedImage(compareSize, compareSize, BufferedImage.TYPE_INT_RGB);
			dataImage.getGraphics().drawImage(compareImage, 0, 0, compareSize, compareSize, null);
		}

		red = new int[compareSize * compareSize];
		green = new int[compareSize * compareSize];
		blue = new int[compareSize * compareSize];

		int rgb, index;

		for (int x = 0; x < compareSize; x++)
		{
			for (int y = 0; y < compareSize; y++)
			{
				rgb = dataImage.getRGB(x, y);

				index = x + compareSize * y;
				red[index] = ImageTools.red(rgb);
				green[index] = ImageTools.green(rgb);
				blue[index] = ImageTools.blue(rgb);
			}
		}
	}

	public abstract int compare(ComparableImage ci);
}
