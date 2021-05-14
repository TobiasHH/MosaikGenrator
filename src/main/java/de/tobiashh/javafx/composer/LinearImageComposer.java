package de.tobiashh.javafx.composer;

import de.tobiashh.javafx.TilesStraightDistance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class LinearImageComposer implements ImageComposer {
    private final static Logger LOGGER = LoggerFactory.getLogger(LinearImageComposer.class.getName());

    private final IndexUpdater indexUpdater;

    private final List<List<Integer>> destinationTileIDs;

    private final int tilesPerRow;
    private final int tilesPerColumn;
    private final int maxReuses;
    private final int reuseDistance;
    private final List<Integer> areaOfInterest;

    public LinearImageComposer(int tilesPerRow, int tilesPerColumn, int maxReuses, int reuseDistance, List<Integer> areaOfInterest, List<List<Integer>> destinationTileIDs) {
        this.tilesPerRow = tilesPerRow;
        this.tilesPerColumn = tilesPerColumn;
        this.maxReuses = maxReuses;
        this.reuseDistance = reuseDistance;
        this.areaOfInterest = areaOfInterest;

        indexUpdater = new IndexUpdater(new TilesStraightDistance(tilesPerRow), maxReuses, reuseDistance);
        this.destinationTileIDs = destinationTileIDs;
    }

    private int mosaikImageIndex(int x, int y) {
        LOGGER.trace("mosaikImageIndex from {},{}", x, y);
        return y * tilesPerRow + x;
    }

    private List<Integer> idListFromIndexMangers(IndexManager[] indexManagers) {
        return Arrays.stream(indexManagers).mapToInt(IndexManager::getDstTileID).boxed().collect(Collectors.toList());
    }

    public List<Integer> generate() {
        LOGGER.info("generateLinearImage");
        IndexManager[] indexManagers = new IndexManager[tilesPerRow * tilesPerColumn];

        for (int y = 0; y < tilesPerColumn; y++) {
            for (int x = 0; x < tilesPerRow; x++) {
                indexManagers[mosaikImageIndex(x, y)] = new IndexManager();
                indexManagers[mosaikImageIndex(x, y)].setDstTileIDs(destinationTileIDs.get(mosaikImageIndex(x,y)));
            }
        }

        for (int y = 0; y < tilesPerColumn * tilesPerRow; y++) {
            for (int x = 0; x < tilesPerRow; x++) {
                if (areaOfInterest.contains(mosaikImageIndex(x, y))) {
                    if (!indexUpdater.setDstTileIndex(mosaikImageIndex(x, y), indexManagers)) return idListFromIndexMangers(indexManagers);
                }
            }
        }

        for (int y = 0; y < tilesPerColumn; y++) {
            for (int x = 0; x < tilesPerRow; x++) {
                if (!areaOfInterest.contains(mosaikImageIndex(x, y))) {
                    if (!indexUpdater.setDstTileIndex(mosaikImageIndex(x, y), indexManagers)) return idListFromIndexMangers(indexManagers);
                }
            }
        }

        return idListFromIndexMangers(indexManagers);
    }
}
