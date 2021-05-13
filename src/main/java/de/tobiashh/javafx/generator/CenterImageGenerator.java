package de.tobiashh.javafx.generator;

import de.tobiashh.javafx.TilesStraightDistance;
import de.tobiashh.javafx.tiles.IndexManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CenterImageGenerator implements ImageGenerator {
    private final static Logger LOGGER = LoggerFactory.getLogger(CenterImageGenerator.class.getName());

    private final IndexUpdater indexUpdater;
    TilesStraightDistance tilesStraightDistance;

    private final IndexManager[] indexManagers;

    private final int tilesPerRow;
    private final int tilesPerColumn;
    private final List<Integer> areaOfInterest;

    public CenterImageGenerator(int tilesPerRow, int tilesPerColumn, int maxReuses, int reuseDistance, List<Integer> areaOfInterest, IndexManager[] indexManagers) {
        this.tilesPerRow = tilesPerRow;
        this.tilesPerColumn = tilesPerColumn;
        this.areaOfInterest = areaOfInterest;
        this.indexManagers = indexManagers;

        tilesStraightDistance = new TilesStraightDistance(tilesPerRow);
        indexUpdater = new IndexUpdater(tilesStraightDistance, maxReuses, reuseDistance);
    }

    private int mosaikImageIndex(int x, int y) {
        LOGGER.trace("mosaikImageIndex from {},{}", x, y);
        return y * tilesPerRow + x;
    }

    public IndexManager[] generate() {
        LOGGER.info("generateCenterDistanceImage");
        IndexManager[] indexManagers = new IndexManager[tilesPerRow * tilesPerColumn];

        for (int y = 0; y < tilesPerColumn; y++) {
            for (int x = 0; x < tilesPerRow; x++) {
                indexManagers[mosaikImageIndex(x, y)] = new IndexManager();
                indexManagers[mosaikImageIndex(x, y)].setDstTileIDs(this.indexManagers[mosaikImageIndex(x,y)].getDstTileIDs());
            }
        }

        List<Integer> tileIndices = IntStream.range(0, tilesPerColumn * tilesPerRow).boxed().collect(Collectors.toList());
        List<Integer> areaOfIntrestIndices = new ArrayList<>(areaOfInterest);
        tileIndices.removeAll(areaOfIntrestIndices);
        int startIndex = mosaikImageIndex(tilesPerRow / 2, tilesPerColumn / 2);

        while (areaOfIntrestIndices.size() > 0) {
            Integer index = areaOfIntrestIndices
                    .stream()
                    .min(Comparator.comparingInt(value -> tilesStraightDistance.calculate(value, startIndex))).get();
            if (!indexUpdater.setDstTileIndex(index, indexManagers)) return indexManagers;
            areaOfIntrestIndices.remove(index);
        }

        while (tileIndices.size() > 0) {
            Integer index = tileIndices.stream()
                    .min(Comparator.comparingInt(value -> tilesStraightDistance.calculate(value, startIndex))).get();
            if (!indexUpdater.setDstTileIndex(index, indexManagers)) return indexManagers;
            tileIndices.remove(index);
        }

        return indexManagers;
    }
}
