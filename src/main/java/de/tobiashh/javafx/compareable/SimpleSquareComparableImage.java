package de.tobiashh.javafx.compareable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleSquareComparableImage extends ComparableImage {
	private final static Logger LOGGER = LoggerFactory.getLogger(SimpleSquareComparableImage.class.getName());

	@Override
	public int compare(ComparableImage ci) {
		LOGGER.debug("compare");
		int result = 0;
		
		result += compareColor(red, ci.red) + compareColor(green, ci.green) + compareColor(blue, ci.blue);
		
		return result;
	}

	private int compareColor(int[] color, int[] color2) {
		LOGGER.trace("compareColor");
		int result = 0;

		for (int i = 0; i < color.length; i++)
		{
			int diff = color[i] - color2[i];
			result += diff * diff;
		}

		return result;
	}
}
