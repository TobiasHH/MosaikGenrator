package de.tobiashh.javafx.composer.impl;

import de.tobiashh.javafx.TilesStraightDistance;
import de.tobiashh.javafx.composer.ImageComposer;
import de.tobiashh.javafx.composer.IndexManager;
import de.tobiashh.javafx.composer.IndexUpdater;
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

    private int mosaikImageIndex(int x, int y, int tilesPerRow) {
        LOGGER.trace("mosaikImageIndex from {},{}", x, y);
        return y * tilesPerRow + x;
    }

    private List<Integer> idListFromIndexMangers(IndexManager[] indexManagers) {
        return Arrays.stream(indexManagers).mapToInt(IndexManager::getDstTileID).boxed().collect(Collectors.toList());
    }

    @Override
    public List<Integer> generate(int tilesPerRow, int tilesPerColumn, int maxReuses, int reuseDistance, List<Integer> areaOfInterest, List<List<Integer>> destinationTileIDs) {
        LOGGER.info("generateRandomImage");

        IndexUpdater indexUpdater = new IndexUpdater(new TilesStraightDistance(tilesPerRow), maxReuses, reuseDistance);

        IndexManager[] indexManagers = new IndexManager[tilesPerRow * tilesPerColumn];

        for (int y = 0; y < tilesPerColumn; y++) {
            for (int x = 0; x < tilesPerRow; x++) {
                indexManagers[mosaikImageIndex(x, y, tilesPerRow)] = new IndexManager();
                indexManagers[mosaikImageIndex(x, y, tilesPerRow)].setDstTileIDs(destinationTileIDs.get(mosaikImageIndex(x,y, tilesPerRow)));
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