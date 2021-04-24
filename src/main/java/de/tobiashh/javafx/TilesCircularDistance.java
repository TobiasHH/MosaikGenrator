package de.tobiashh.javafx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TilesCircularDistance {
    private final static Logger LOGGER = LoggerFactory.getLogger(TilesCircularDistance.class.getName());

    private final int tilesPerRow;

    public TilesCircularDistance(int tilesPerRow)
    {
        this.tilesPerRow = tilesPerRow;
    }

    public int calculate(int index1, int index2){
        if(index1 == index2) return 0;
        int distanceX = getTilePositionX(index1) - getTilePositionX(index2);
        int distanceY = getTilePositionY(index1) - getTilePositionY(index2);
        int distance = (int) Math.sqrt(distanceX * distanceX + distanceY * distanceY);
        LOGGER.debug("getTileDistance - index {}, index {} -> distance {}", index1, index2, distance);
        return distance;
    }

    private int getTilePositionX(int index) {
        int tilePositionX = index % tilesPerRow;
        LOGGER.trace("getTilePositionX index {} -> posX {}", index, tilePositionX);
        return tilePositionX;
    }

    private int getTilePositionY(int index) {
        int tilePositionY = index / tilesPerRow;
        LOGGER.trace("getTilePositionX index {} -> posY {}", index, tilePositionY);
        return tilePositionY;
    }
}
