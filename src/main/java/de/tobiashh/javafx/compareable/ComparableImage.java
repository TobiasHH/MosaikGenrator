package de.tobiashh.javafx.compareable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;

public abstract class ComparableImage {
	private final static Logger LOGGER = LoggerFactory.getLogger(ComparableImage.class.getName());

	protected BufferedImage compareImage;

	protected void setDataImage(BufferedImage dataImage, int compareSize) {
		LOGGER.trace("setDataImage");
		this.compareImage = getCompareImage(dataImage, compareSize);
	}

	private BufferedImage getCompareImage(BufferedImage compareImage, int compareSize) {
		if(compareImage.getWidth() != compareSize || compareImage.getHeight() != compareSize) {
			BufferedImage resizedCompareImage = new BufferedImage(compareSize, compareSize, BufferedImage.TYPE_INT_RGB);
			resizedCompareImage.getGraphics().drawImage(compareImage, 0, 0, compareSize, compareSize, null);
			return resizedCompareImage;
		}
		return compareImage;
	}

	public abstract int compare(ComparableImage ci, int colorAlignment);
}
