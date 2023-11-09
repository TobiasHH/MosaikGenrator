package distanceCalculator;

public class StraightTileDistanceCalculator extends DistanceCalculator {

    public StraightTileDistanceCalculator(int tilesPerRow) {
        super(tilesPerRow);
    }

    public int calculate(int index1, int index2) {
        if (index1 == index2) return 0;
        int distance = Math.abs(getTilePositionX(index1) - getTilePositionX(index2)) + Math.abs(getTilePositionY(index1) - getTilePositionY(index2));
        LOGGER.debug("getTileDistance - index {}, index {} -> distance {}", index1, index2, distance);
        return distance;
    }
}
