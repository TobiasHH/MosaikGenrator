package de.tobiashh.javafx.composer.impl;

import de.tobiashh.javafx.TilesStraightDistance;
import de.tobiashh.javafx.composer.ImageComposer;
import de.tobiashh.javafx.composer.IndexManager;
import de.tobiashh.javafx.composer.IndexUpdater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CenterImageComposer implements ImageComposer {
    private final static Logger LOGGER = LoggerFactory.getLogger(CenterImageComposer.class.getName());

    private int mosaikImageIndex(int x, int y, int tilesPerRow) {
        LOGGER.trace("mosaikImageIndex from {},{}", x, y);
        return y * tilesPerRow + x;
    }

    private List<Integer> idListFromIndexMangers(IndexManager[] indexManagers) {
        return Arrays.stream(indexManagers).mapToInt(IndexManager::getDstTileID).boxed().collect(Collectors.toList());
    }

    @Override
    public List<Integer> generate(int tilesPerRow, int tilesPerColumn, int maxReuses, int reuseDistance, List<Integer> areaOfInterest, List<List<Integer>> destinationTileIDs) {
        LOGGER.info("generateCenterDistanceImage");

        TilesStraightDistance tilesStraightDistance = new TilesStraightDistance(tilesPerRow);
        IndexUpdater indexUpdater = new IndexUpdater(tilesStraightDistance, maxReuses, reuseDistance);

        IndexManager[] indexManagers = new IndexManager[tilesPerRow * tilesPerColumn];

        for (int y = 0; y < tilesPerColumn; y++) {
            for (int x = 0; x < tilesPerRow; x++) {
                indexManagers[mosaikImageIndex(x, y, tilesPerRow)] = new IndexManager();
                indexManagers[mosaikImageIndex(x, y, tilesPerRow)].setDstTileIDs(destinationTileIDs.get(mosaikImageIndex(x,y, tilesPerRow)));
            }
        }

        List<Integer> tileIndices = IntStream.range(0, tilesPerColumn * tilesPerRow).boxed().collect(Collectors.toList());
        List<Integer> areaOfIntrestIndices = new ArrayList<>(areaOfInterest);
        tileIndices.removeAll(areaOfIntrestIndices);
        int startIndex = mosaikImageIndex(tilesPerRow / 2, tilesPerColumn / 2, tilesPerRow);

        while (areaOfIntrestIndices.size() > 0) {
            Integer index = areaOfIntrestIndices
                    .stream()
                    .min(Comparator.comparingInt(value -> tilesStraightDistance.calculate(value, startIndex))).get();
            if (indexUpdater.setDstTileIndex(index, indexManagers)) return idListFromIndexMangers(indexManagers);
            areaOfIntrestIndices.remove(index);
        }

        while (tileIndices.size() > 0) {
            Integer index = tileIndices.stream()
                    .min(Comparator.comparingInt(value -> tilesStraightDistance.calculate(value, startIndex))).get();
            if (indexUpdater.setDstTileIndex(index, indexManagers)) return idListFromIndexMangers(indexManagers);
            tileIndices.remove(index);
        }

        return idListFromIndexMangers(indexManagers);
    }
}