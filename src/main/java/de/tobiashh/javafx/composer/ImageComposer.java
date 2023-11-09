package de.tobiashh.javafx.composer;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public abstract class ImageComposer {
    public abstract List<Integer> generate(int tilesPerRow, int tilesPerColumn, int maxReuses, int reuseDistance, List<Integer> areaOfInterest, List<List<Integer>> destinationTileIDs);

    Integer[] fillImage(int tilesPerRow, int tilesPerColumn, int reuseDistance, int maxReuses, List<List<Integer>> destinationTileIDs, List<Integer> indices) {
        ReuseableChecker reuseableChecker = new ReuseableChecker(tilesPerRow, tilesPerColumn, reuseDistance);
        Integer[] returnValue = new Integer[tilesPerRow * tilesPerColumn];
        Arrays.fill(returnValue, -1);

        while (indices.size() > 0) {
            Integer index = indices.remove(0);
            for (Integer id : destinationTileIDs.get(index)) {
                if (isUsedLessThenMaxReuses(id, returnValue, maxReuses) && reuseableChecker.isReuseableAtPosition(id, returnValue, index)) {
                    returnValue[index] = id;
                    break;
                }
            }
        }

        return returnValue;
    }

    private boolean isUsedLessThenMaxReuses(int id, Integer[] imageIds, int maxReuses) {
        return Arrays.stream(imageIds).filter(imageID -> imageID == id).count() <= maxReuses;
    }

    List<Integer> getIntegerList(int tilesPerRow, int tilesPerColumn) {
        return IntStream.range(0, tilesPerColumn * tilesPerRow).boxed().collect(Collectors.toList());
    }
}
