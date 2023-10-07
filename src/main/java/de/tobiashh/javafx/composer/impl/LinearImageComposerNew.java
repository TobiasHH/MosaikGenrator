package de.tobiashh.javafx.composer.impl;

import de.tobiashh.javafx.TilesStraightDistance;
import de.tobiashh.javafx.composer.ImageComposer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class LinearImageComposerNew implements ImageComposer {
    private final static Logger LOGGER = LoggerFactory.getLogger(LinearImageComposerNew.class.getName());

    private int maxReuses;

    @Override
    public List<Integer> generate(int tilesPerRow, int tilesPerColumn, int maxReuses, int reuseDistance, List<Integer> areaOfInterest, List<List<Integer>> destinationTileIDs) {
        ReuseableChecker reuseableChecker = new ReuseableChecker(tilesPerRow, tilesPerColumn, reuseDistance);
        this.maxReuses = maxReuses;
        LOGGER.info("generateLinearImage");

        int[] returnValue = new int[tilesPerRow * tilesPerColumn];
        Arrays.fill(returnValue, -1);

        List<Integer> tileIndices = IntStream.range(0, tilesPerColumn * tilesPerRow).boxed().collect(Collectors.toList());
        List<Integer> indices = new ArrayList<>(areaOfInterest);

        tileIndices.removeAll(indices);
        indices.addAll(tileIndices);

        while (indices.size() > 0) {
            Integer index = indices.remove(0);
            List<Integer> idList = destinationTileIDs.get(index);
            for (Integer id : idList) {
                if(isUsedLessThenMaxReuses(id, returnValue) && reuseableChecker.isReuseableAtPosition(id, returnValue, index))
                {
                    returnValue[index] = id;
                    break;
                }
            }
        }

        return Arrays.stream(returnValue).boxed().collect(Collectors.toList());
    }

    private boolean isUsedLessThenMaxReuses(int id, int[] imageIds)
    {
        return Arrays.stream(imageIds).parallel().filter(imageID -> imageID == id).count() <= maxReuses;
    }
}
