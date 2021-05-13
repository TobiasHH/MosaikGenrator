package de.tobiashh.javafx.generator;

import de.tobiashh.javafx.TilesStraightDistance;
import de.tobiashh.javafx.tiles.IndexManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RandomImageGenerator implements ImageGenerator {
    private final static Logger LOGGER = LoggerFactory.getLogger(RandomImageGenerator.class.getName());

    private final IndexUpdater indexUpdater;

    private final IndexManager[] indexManagers;

    private final int tilesPerRow;
    private final int tilesPerColumn;
    private final int maxReuses;
    private final int reuseDistance;
    private final List<Integer> areaOfInterest;

    public RandomImageGenerator(int tilesPerRow, int tilesPerColumn, int maxReuses, int reuseDistance, List<Integer> areaOfInterest, IndexManager[] indexManagers) {
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
        LOGGER.info("generateRandomImage");
        IndexManager[] indexManagers = new IndexManager[tilesPerRow * tilesPerColumn];

        for (int y = 0; y < tilesPerColumn; y++) {
            for (int x = 0; x < tilesPerRow; x++) {
                indexManagers[mosaikImageIndex(x, y)] = new IndexManager();
                indexManagers[mosaikImageIndex(x, y)].setDstTileIDs(this.indexManagers[mosaikImageIndex(x,y)].getDstTileIDs());
            }
        }

        Random rand = new Random();

        List<Integer> tileIndices = IntStream.range(0, tilesPerColumn * tilesPerRow).boxed().collect(Collectors.toList());
        List<Integer> areaOfInterestIndices = new ArrayList<>(areaOfInterest);
        tileIndices.removeAll(areaOfInterestIndices);

        while (areaOfInterestIndices.size() > 0) {
            if (!indexUpdater.setDstTileIndex(areaOfInterestIndices.remove(rand.nextInt(areaOfInterestIndices.size())), indexManagers))
                return indexManagers;
        }

        while (tileIndices.size() > 0) {
            if (!indexUpdater.setDstTileIndex(tileIndices.remove(rand.nextInt(tileIndices.size())), indexManagers))
                return indexManagers;
        }

        return indexManagers;
    }
}
