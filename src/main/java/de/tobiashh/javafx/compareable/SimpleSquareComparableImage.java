package de.tobiashh.javafx.compareable;

import de.tobiashh.javafx.properties.PropertiesManager;

import java.util.logging.Logger;

public class SimpleSquareComparableImage extends ComparableImage {
	private final static Logger LOGGER = Logger.getLogger(SimpleSquareComparableImage.class.getName());

	@Override
	public int compare(ComparableImage ci) {
		LOGGER.info("SimpleSquareComparableImage.compare");
		int result = 0;
		
		result += compareColor(red, ci.red) + compareColor(green, ci.green) + compareColor(blue, ci.blue);
		
		return result;
	}
	
	private int compareColor(int[] color, int[] color2) {
		LOGGER.info("SimpleSquareComparableImage.compareColor");
		int result = 0;
		
		for (int i = 0; i < color.length; i++)
		{
			int diff = color[i] - color2[i];
			result += diff * diff;
		}
		
		return result;
	}
}
