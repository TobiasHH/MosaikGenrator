package de.tobiashh.javafx.composer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class RandomImageComposer extends ImageComposer {
    private final static Logger LOGGER = LoggerFactory.getLogger(RandomImageComposer.class.getName());

    @Override
    public List<Integer> generate(int tilesPerRow, int tilesPerColumn, int maxReuses, int reuseDistance, List<Integer> areaOfInterest, List<List<Integer>> destinationTileIDs) {
        LOGGER.info("generateRandomImage");

        List<Integer> tileIndices = randomize(getIntegerList(tilesPerRow, tilesPerColumn));
        List<Integer> indices = randomize(new ArrayList<>(areaOfInterest));

        tileIndices.removeAll(indices);
        indices.addAll(tileIndices);

        return Arrays.asList(fillImage(tilesPerRow, tilesPerColumn, reuseDistance, maxReuses, destinationTileIDs, indices));
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
