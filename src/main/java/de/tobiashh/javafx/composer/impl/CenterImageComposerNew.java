package de.tobiashh.javafx.composer.impl;

import de.tobiashh.javafx.TilesStraightDistance;
import de.tobiashh.javafx.composer.ImageComposer;
import de.tobiashh.javafx.composer.ReuseableChecker;
import de.tobiashh.javafx.tools.Index2D;
import de.tobiashh.javafx.tools.IndexConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CenterImageComposerNew extends ImageComposer {
    private final static Logger LOGGER = LoggerFactory.getLogger(CenterImageComposerNew.class.getName());

    @Override
    public List<Integer> generate(int tilesPerRow, int tilesPerColumn, int maxReuses, int reuseDistance, List<Integer> areaOfInterest, List<List<Integer>> destinationTileIDs) {
        LOGGER.info("generateLinearImage");

        int startIndex = new IndexConverter(tilesPerRow).convert2DToLinear(new Index2D(tilesPerRow / 2, tilesPerColumn / 2));
        TilesStraightDistance tilesStraightDistance = new TilesStraightDistance(tilesPerRow);

        List<Integer> tileIndices = sort(IntStream.range(0, tilesPerColumn * tilesPerRow).boxed().collect(Collectors.toList()), tilesStraightDistance, startIndex);
        List<Integer> indices = sort(areaOfInterest, tilesStraightDistance, startIndex);
        tileIndices.removeAll(indices);
        indices.addAll(tileIndices);

        return Arrays.stream(fillImage(tilesPerRow, tilesPerColumn, reuseDistance, maxReuses, destinationTileIDs, indices)).boxed().collect(Collectors.toList());
    }

    private List<Integer> sort(List<Integer> list, TilesStraightDistance tilesStraightDistance, int startIndex) {
        return list.stream().sorted(Comparator.comparingInt(value -> tilesStraightDistance.calculate(value, startIndex))).collect(Collectors.toList());
    }
}
