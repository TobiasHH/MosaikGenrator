package de.tobiashh.javafx.composer;

import de.tobiashh.javafx.TilesStraightDistance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RandomImageComposer implements ImageComposer {
    private final static Logger LOGGER = LoggerFactory.getLogger(RandomImageComposer.class.getName());

    private final IndexUpdater indexUpdater;

    private final List<List<Integer>> destinationTileIDs;

    private final int tilesPerRow;
    private final int tilesPerColumn;
    private final List<Integer> areaOfInterest;

    public RandomImageComposer(int tilesPerRow, int tilesPerColumn, int maxReuses, int reuseDistance, List<Integer> areaOfInterest, List<List<Integer>> destinationTileIDs) {
        this.tilesPerRow = tilesPerRow;
        this.tilesPerColumn = tilesPerColumn;
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
        LOGGER.info("generateRandomImage");
        IndexManager[] indexManagers = new IndexManager[tilesPerRow * tilesPerColumn];

        for (int y = 0; y < tilesPerColumn; y++) {
            for (int x = 0; x < tilesPerRow; x++) {
                indexManagers[mosaikImageIndex(x, y)] = new IndexManager();
                indexManagers[mosaikImageIndex(x, y)].setDstTileIDs(destinationTileIDs.get(mosaikImageIndex(x,y)));
            }
        }

        Random rand = new Random();

        List<Integer> tileIndices = IntStream.range(0, tilesPerColumn * tilesPerRow).boxed().collect(Collectors.toList());
        List<Integer> areaOfInterestIndices = new ArrayList<>(areaOfInterest);
        tileIndices.removeAll(areaOfInterestIndices);

        while (areaOfInterestIndices.size() > 0) {
            if (indexUpdater.setDstTileIndex(areaOfInterestIndices.remove(rand.nextInt(areaOfInterestIndices.size())), indexManagers)) {
                idListFromIndexMangers(indexManagers);
            }
        }

        while (tileIndices.size() > 0) {
            if (indexUpdater.setDstTileIndex(tileIndices.remove(rand.nextInt(tileIndices.size())), indexManagers)){
                idListFromIndexMangers(indexManagers);
            }
        }

        return idListFromIndexMangers(indexManagers);
    }
}
