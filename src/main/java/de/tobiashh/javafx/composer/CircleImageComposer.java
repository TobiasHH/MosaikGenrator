package de.tobiashh.javafx.composer;

import de.tobiashh.javafx.tools.Converter;
import de.tobiashh.javafx.tools.Position;
import distanceCalculator.CircularTileDistanceCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class CircleImageComposer extends ImageComposer {
    private final static Logger LOGGER = LoggerFactory.getLogger(CircleImageComposer.class.getName());

    @Override
    public List<Integer> generate(int tilesPerRow, int tilesPerColumn, int maxReuses, int reuseDistance, List<Integer> areaOfInterest, List<List<Integer>> destinationTileIDs) {
        LOGGER.info("generateCircleImage");

        int startIndex = new Converter(tilesPerRow).getIndex(new Position(tilesPerRow / 2, tilesPerColumn / 2));
        CircularTileDistanceCalculator circularTileDistanceCalculator = new CircularTileDistanceCalculator(tilesPerRow);

        List<Integer> tileIndices = sort(getIntegerList(tilesPerRow, tilesPerColumn), circularTileDistanceCalculator, startIndex);
        List<Integer> indices = sort(areaOfInterest, circularTileDistanceCalculator, startIndex);

        tileIndices.removeAll(indices);
        indices.addAll(tileIndices);

        return Arrays.asList(fillImage(tilesPerRow, tilesPerColumn, reuseDistance, maxReuses, destinationTileIDs, indices));
    }

    private List<Integer> sort(List<Integer> list, CircularTileDistanceCalculator circularTileDistanceCalculator, int startIndex) {
        return list.stream().sorted(Comparator.comparingInt(value -> circularTileDistanceCalculator.calculate(value, startIndex))).collect(Collectors.toList());
    }
}
