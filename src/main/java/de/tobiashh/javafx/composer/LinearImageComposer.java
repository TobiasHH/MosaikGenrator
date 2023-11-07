package de.tobiashh.javafx.composer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class LinearImageComposer extends ImageComposer {
    private final static Logger LOGGER = LoggerFactory.getLogger(LinearImageComposer.class.getName());

    @Override
    public List<Integer> generate(int tilesPerRow, int tilesPerColumn, int maxReuses, int reuseDistance, List<Integer> areaOfInterest, List<List<Integer>> destinationTileIDs) {
        LOGGER.info("generateLinearImage");

        List<Integer> tileIndices = getIntegerList(tilesPerRow, tilesPerColumn);
        List<Integer> indices = new ArrayList<>(areaOfInterest);

        tileIndices.removeAll(indices);
        indices.addAll(tileIndices);

        return Arrays.asList(fillImage(tilesPerRow, tilesPerColumn, reuseDistance, maxReuses, destinationTileIDs, indices));
    }
}
