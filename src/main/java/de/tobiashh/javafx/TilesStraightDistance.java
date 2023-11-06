package de.tobiashh.javafx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TilesStraightDistance {
    private final static Logger LOGGER = LoggerFactory.getLogger(TilesStraightDistance.class.getName());

    private final int tilesPerRow;

    public TilesStraightDistance(int tilesPerRow)
    {
        this.tilesPerRow = tilesPerRow;
    }

    public int calculate(int index1, int index2){
        if(index1 == index2) return 0;
        int distance = Math.abs(getTilePositionX(index1) - getTilePositionX(index2)) + Math.abs(getTilePositionY(index1) - getTilePositionY(index2));
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
