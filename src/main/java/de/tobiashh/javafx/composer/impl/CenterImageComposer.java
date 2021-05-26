package de.tobiashh.javafx.composer.impl;

import de.tobiashh.javafx.TilesStraightDistance;
import de.tobiashh.javafx.composer.ImageComposer;
import de.tobiashh.javafx.composer.IndexManager;
import de.tobiashh.javafx.composer.IndexUpdater;
import de.tobiashh.javafx.tools.Index2D;
import de.tobiashh.javafx.tools.IndexConverter;
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

    private List<Integer> idListFromIndexMangers(IndexManager[] indexManagers) {
        return Arrays.stream(indexManagers).mapToInt(IndexManager::getDstTileID).boxed().collect(Collectors.toList());
    }

    @Override
    public List<Integer> generate(int tilesPerRow, int tilesPerColumn, int maxReuses, int reuseDistance, List<Integer> areaOfInterest, List<List<Integer>> destinationTileIDs) {
        LOGGER.info("generateCenterDistanceImage");
        IndexConverter indexConverter = new IndexConverter(tilesPerRow);

        TilesStraightDistance tilesStraightDistance = new TilesStraightDistance(tilesPerRow);
        IndexUpdater indexUpdater = new IndexUpdater(tilesStraightDistance, maxReuses, reuseDistance);

        IndexManager[] indexManagers = new IndexManager[tilesPerRow * tilesPerColumn];

        for (int index = 0; index < tilesPerColumn * tilesPerRow; index++) {
            indexManagers[index] = new IndexManager();
            indexManagers[index].setDstTileIDs(destinationTileIDs.get(index));
        }

        // TODO die Composer unterscheiden sich nur noch von der Sortierung der Index Liste

        List<Integer> tileIndices = IntStream.range(0, tilesPerColumn * tilesPerRow).boxed().collect(Collectors.toList());
        List<Integer> areaOfInterestIndices = new ArrayList<>(areaOfInterest);
        tileIndices.removeAll(areaOfInterestIndices);

        int startIndex = indexConverter.convert2DToLinear(new Index2D(tilesPerRow / 2, tilesPerColumn / 2));

        tileIndices = sort(tileIndices, tilesStraightDistance, startIndex);
        areaOfInterestIndices = sort(areaOfInterestIndices, tilesStraightDistance, startIndex);

        while (areaOfInterestIndices.size() > 0) {
            Integer index = areaOfInterestIndices.remove(0);
            if (indexUpdater.setDstTileIndex(index, indexManagers)) return idListFromIndexMangers(indexManagers);
        }

        while (tileIndices.size() > 0) {
            Integer index = tileIndices.remove(0);
            if (indexUpdater.setDstTileIndex(index, indexManagers)) return idListFromIndexMangers(indexManagers);
        }

        return idListFromIndexMangers(indexManagers);
    }

    private List<Integer> sort(List<Integer> list, TilesStraightDistance tilesStraightDistance, int startIndex) {
        return list.stream().sorted(Comparator.comparingInt(value -> tilesStraightDistance.calculate(value, startIndex))).collect(Collectors.toList());
    }
}
