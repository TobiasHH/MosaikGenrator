package de.tobiashh.javafx.composer;

import distanceCalculator.StraightTileDistanceCalculator;
import de.tobiashh.javafx.tools.Position;
import de.tobiashh.javafx.tools.IndexConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CenterImageComposer extends ImageComposer {
    private final static Logger LOGGER = LoggerFactory.getLogger(CenterImageComposer.class.getName());

    @Override
    public List<Integer> generate(int tilesPerRow, int tilesPerColumn, int maxReuses, int reuseDistance, List<Integer> areaOfInterest, List<List<Integer>> destinationTileIDs) {
        LOGGER.info("generateCenterImage");

        int startIndex = new IndexConverter(tilesPerRow).convert2DToLinear(new Position(tilesPerRow / 2, tilesPerColumn / 2));
        StraightTileDistanceCalculator straightTileDistanceCalculator = new StraightTileDistanceCalculator(tilesPerRow);

        List<Integer> tileIndices = sort(IntStream.range(0, tilesPerColumn * tilesPerRow).boxed().collect(Collectors.toList()), straightTileDistanceCalculator, startIndex);
        List<Integer> indices = sort(areaOfInterest, straightTileDistanceCalculator, startIndex);
        tileIndices.removeAll(indices);
        indices.addAll(tileIndices);

        return Arrays.stream(fillImage(tilesPerRow, tilesPerColumn, reuseDistance, maxReuses, destinationTileIDs, indices)).boxed().collect(Collectors.toList());
    }

    private List<Integer> sort(List<Integer> list, StraightTileDistanceCalculator straightTileDistanceCalculator, int startIndex) {
        return list.stream().sorted(Comparator.comparingInt(value -> straightTileDistanceCalculator.calculate(value, startIndex))).collect(Collectors.toList());
    }
}
