package de.tobiashh.javafx.composer.impl;

import de.tobiashh.javafx.composer.ImageComposer;
import de.tobiashh.javafx.composer.ReuseableChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RandomImageComposerNew extends ImageComposer {
    private final static Logger LOGGER = LoggerFactory.getLogger(RandomImageComposerNew.class.getName());

    @Override
    public List<Integer> generate(int tilesPerRow, int tilesPerColumn, int maxReuses, int reuseDistance, List<Integer> areaOfInterest, List<List<Integer>> destinationTileIDs) {
        LOGGER.info("generateLinearImage");

        List<Integer> tileIndices = randomize(IntStream.range(0, tilesPerColumn * tilesPerRow).boxed().collect(Collectors.toList()));
        List<Integer> indices = randomize(new ArrayList<>(areaOfInterest));
        tileIndices.removeAll(indices);
        indices.addAll(tileIndices);

        return Arrays.stream(fillImage(tilesPerRow, tilesPerColumn, reuseDistance, maxReuses, destinationTileIDs, indices)).boxed().collect(Collectors.toList());
    }

    private List<Integer> randomize(List<Integer> indices) {
        List<Integer> retval = new ArrayList<>();
        Random rand = new Random();

        while (indices.size() > 0) {
            retval.add(indices.remove(rand.nextInt(indices.size())));
        }

        return retval;
    }
}
