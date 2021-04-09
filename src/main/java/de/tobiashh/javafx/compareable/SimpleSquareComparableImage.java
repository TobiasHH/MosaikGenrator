package de.tobiashh.javafx.compareable;

public class SimpleSquareComparableImage extends ComparableImage {

	@Override
	public int compare(ComparableImage ci) {
		int result = 0;
		
		result += compareColor(red, ci.red) + compareColor(green, ci.green) + compareColor(blue, ci.blue);
		
		return result;
	}
	
	private int compareColor(int[] color, int[] color2) {
		int result = 0;
		
		for (int i = 0; i < color.length; i++)
		{
			int diff = color[i] - color2[i];
			result += diff * diff;
		}
		
		return result;
	}
}
