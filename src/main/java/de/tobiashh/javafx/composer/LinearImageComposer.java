package de.tobiashh.javafx.composer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class LinearImageComposer extends ImageComposer {
    private final static Logger LOGGER = LoggerFactory.getLogger(LinearImageComposer.class.getName());

    @Override
    public List<Integer> generate(int tilesPerRow, int tilesPerColumn, int maxReuses, int reuseDistance, List<Integer> areaOfInterest, List<List<Integer>> destinationTileIDs) {
        LOGGER.info("generateLinearImage");

        List<Integer> tileIndices = IntStream.range(0, tilesPerColumn * tilesPerRow).boxed().collect(Collectors.toList());
        List<Integer> indices = new ArrayList<>(areaOfInterest);

        tileIndices.removeAll(indices);
        indices.addAll(tileIndices);

        return Arrays.stream(fillImage(tilesPerRow, tilesPerColumn, reuseDistance, maxReuses, destinationTileIDs, indices)).boxed().collect(Collectors.toList());
    }
}
