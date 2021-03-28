package de.tobiashh.javafx.compareable;

import de.tobiashh.javafx.tools.ImageTools;
import de.tobiashh.javafx.properties.Properties;

import java.awt.image.BufferedImage;

public abstract class ComparableImage {

	protected int[] red;
	protected int[] green;
	protected int[] blue;
	
	public static final int COMPARE_SIZE = Properties.getInstance().getCompareSize();
	
	public void setDataImage(BufferedImage dataImage) {
		calculateData(dataImage);
	}

	protected void calculateData(BufferedImage compareImage) {
		BufferedImage dataImage = compareImage;

		if(compareImage.getWidth() != COMPARE_SIZE || compareImage.getHeight() != COMPARE_SIZE) {
			dataImage = new BufferedImage(COMPARE_SIZE, COMPARE_SIZE, BufferedImage.TYPE_INT_RGB);
			dataImage.getGraphics().drawImage(compareImage, 0, 0, COMPARE_SIZE, COMPARE_SIZE, null);
		}

		red = new int[COMPARE_SIZE * COMPARE_SIZE];
		green = new int[COMPARE_SIZE * COMPARE_SIZE];
		blue = new int[COMPARE_SIZE * COMPARE_SIZE];

		int rgb, index;

		for (int x = 0; x < COMPARE_SIZE; x++)
		{
			for (int y = 0; y < COMPARE_SIZE; y++)
			{
				rgb = dataImage.getRGB(x, y);

				index = x + COMPARE_SIZE * y;
				red[index] = ImageTools.red(rgb);
				green[index] = ImageTools.green(rgb);
				blue[index] = ImageTools.blue(rgb);
			}
		}
	}

	public abstract int compare(ComparableImage ci);
}
