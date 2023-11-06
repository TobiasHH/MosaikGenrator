package distanceCalculator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class DistanceCalculator {
    final static Logger LOGGER = LoggerFactory.getLogger(DistanceCalculator.class.getName());

    private final int tilesPerRow;

    DistanceCalculator(int tilesPerRow) {
        this.tilesPerRow = tilesPerRow;
    }

    public abstract int calculate(int index1, int index2);

    int getTilePositionX(int index) {
        int tilePositionX = index % tilesPerRow;
        LOGGER.trace("getTilePositionX index {} -> posX {}", index, tilePositionX);
        return tilePositionX;
    }

    int getTilePositionY(int index) {
        int tilePositionY = index / tilesPerRow;
        LOGGER.trace("getTilePositionX index {} -> posY {}", index, tilePositionY);
        return tilePositionY;
    }
}