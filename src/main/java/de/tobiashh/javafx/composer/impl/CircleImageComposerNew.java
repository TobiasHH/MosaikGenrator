package de.tobiashh.javafx.composer.impl;

import de.tobiashh.javafx.TilesCircularDistance;
import de.tobiashh.javafx.composer.ImageComposer;
import de.tobiashh.javafx.tools.Index2D;
import de.tobiashh.javafx.tools.IndexConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CircleImageComposerNew extends ImageComposer {
    private final static Logger LOGGER = LoggerFactory.getLogger(CircleImageComposerNew.class.getName());

    @Override
    public List<Integer> generate(int tilesPerRow, int tilesPerColumn, int maxReuses, int reuseDistance, List<Integer> areaOfInterest, List<List<Integer>> destinationTileIDs) {
        LOGGER.info("generateCircleImage");

        int startIndex = new IndexConverter(tilesPerRow).convert2DToLinear(new Index2D(tilesPerRow / 2, tilesPerColumn / 2));
        TilesCircularDistance tilesCircularDistance = new TilesCircularDistance(tilesPerRow);

        List<Integer> tileIndices = sort( IntStream.range(0, tilesPerColumn * tilesPerRow).boxed().collect(Collectors.toList()), tilesCircularDistance, startIndex);
        List<Integer> indices =  sort(areaOfInterest, tilesCircularDistance, startIndex);
        tileIndices.removeAll(indices);
        indices.addAll(tileIndices);

        return Arrays.stream(fillImage(tilesPerRow, tilesPerColumn, reuseDistance, maxReuses, destinationTileIDs, indices)).boxed().collect(Collectors.toList());
    }

    private List<Integer> sort(List<Integer> list, TilesCircularDistance tilesCircularDistance, int startIndex) {
        return list.stream().sorted(Comparator.comparingInt(value -> tilesCircularDistance.calculate(value, startIndex))).collect(Collectors.toList());
    }
}
