package de.tobiashh.javafx.compareable;

import de.tobiashh.javafx.tools.ImageTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;

public class SimpleSquareComparableImage extends ComparableImage {
	private final static Logger LOGGER = LoggerFactory.getLogger(SimpleSquareComparableImage.class.getName());

	@Override
	public int compare(ComparableImage ci, int colorAlignment) {
		LOGGER.debug("compare");
		return colorAlignment > 0 ? compareColors(ImageTools.colorAlignment(this.compareImage, ci.compareImage, colorAlignment), ci.compareImage) : compareColors(compareImage, ci.compareImage);
	}

	private int compareColors(BufferedImage image1, BufferedImage image2) {
		LOGGER.trace("compareColor");
		int result = 0;
		int rgb1, rgb2, diffRed, diffGreen, diffBlue;

		for (int x = 0; x < image1.getWidth(); x++) {
			for (int y = 0; y < image1.getHeight(); y++) {
				rgb1 = image1.getRGB(x, y);
				rgb2 = image2.getRGB(x, y);

				diffRed = ImageTools.red(rgb1) - ImageTools.red(rgb2);
				diffGreen = ImageTools.green(rgb1) - ImageTools.green(rgb2);
				diffBlue = ImageTools.blue(rgb1) - ImageTools.blue(rgb2);
				result += diffRed * diffRed + diffGreen * diffGreen + diffBlue * diffBlue;
			}
		}
		return result;
	}
}
