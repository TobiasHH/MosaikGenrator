package distanceCalculator;

public class CircularTileDistanceCalculator extends DistanceCalculator {

    public CircularTileDistanceCalculator(int tilesPerRow) {
        super(tilesPerRow);
    }

    public int calculate(int index1, int index2) {
        if (index1 == index2) return 0;
        int distanceX = getTilePositionX(index1) - getTilePositionX(index2);
        int distanceY = getTilePositionY(index1) - getTilePositionY(index2);
        int distance = (int) Math.sqrt(distanceX * distanceX + distanceY * distanceY);
        LOGGER.debug("getTileDistance - index {}, index {} -> distance {}", index1, index2, distance);
        return distance;
    }
}
