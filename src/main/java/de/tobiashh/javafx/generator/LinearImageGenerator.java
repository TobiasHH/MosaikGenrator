package de.tobiashh.javafx.generator;

import de.tobiashh.javafx.TilesStraightDistance;
import de.tobiashh.javafx.tiles.IndexManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class LinearImageGenerator implements ImageGenerator{
    private final static Logger LOGGER = LoggerFactory.getLogger(LinearImageGenerator.class.getName());

    private final IndexUpdater indexUpdater;

    private final IndexManager[] indexManagers;

    private final int tilesPerRow;
    private final int tilesPerColumn;
    private final int maxReuses;
    private final int reuseDistance;
    private final List<Integer> areaOfInterest;

    public LinearImageGenerator(int tilesPerRow, int tilesPerColumn, int maxReuses, int reuseDistance, List<Integer> areaOfInterest, IndexManager[] indexManagers) {
        this.tilesPerRow = tilesPerRow;
        this.tilesPerColumn = tilesPerColumn;
        this.maxReuses = maxReuses;
        this.reuseDistance = reuseDistance;
        this.areaOfInterest = areaOfInterest;

        indexUpdater = new IndexUpdater(new TilesStraightDistance(tilesPerRow), maxReuses, reuseDistance);
        this.indexManagers = indexManagers;
    }

    private int mosaikImageIndex(int x, int y) {
        LOGGER.trace("mosaikImageIndex from {},{}", x, y);
        return y * tilesPerRow + x;
    }

    public IndexManager[] generate() {
        LOGGER.info("generateLinearImage");
        IndexManager[] indexManagers = new IndexManager[tilesPerRow * tilesPerColumn];

        for (int y = 0; y < tilesPerColumn; y++) {
            for (int x = 0; x < tilesPerRow; x++) {
                indexManagers[mosaikImageIndex(x, y)] = new IndexManager();
                indexManagers[mosaikImageIndex(x, y)].setDstTileIDs(this.indexManagers[mosaikImageIndex(x,y)].getDstTileIDs());
            }
        }

        for (int y = 0; y < tilesPerColumn; y++) {
            for (int x = 0; x < tilesPerRow; x++) {
                if (areaOfInterest.contains(mosaikImageIndex(x, y))) {
                    if (!indexUpdater.setDstTileIndex(mosaikImageIndex(x, y), indexManagers)) return indexManagers;
                }
            }
        }

        for (int y = 0; y < tilesPerColumn; y++) {
            for (int x = 0; x < tilesPerRow; x++) {
                if (!areaOfInterest.contains(mosaikImageIndex(x, y))) {
                    if (!indexUpdater.setDstTileIndex(mosaikImageIndex(x, y), indexManagers)) return indexManagers;
                }
            }
        }

        return indexManagers;
    }
}
