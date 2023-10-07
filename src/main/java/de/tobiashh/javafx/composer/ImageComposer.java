package de.tobiashh.javafx.composer;

import java.util.Arrays;
import java.util.List;

public abstract class ImageComposer {
    public abstract List<Integer> generate(int tilesPerRow, int tilesPerColumn, int maxReuses, int reuseDistance, List<Integer> areaOfInterest, List<List<Integer>> destinationTileIDs);

    protected int[] fillImage(int tilesPerRow, int tilesPerColumn, int reuseDistance, int maxReuses, List<List<Integer>> destinationTileIDs, List<Integer> indices) {
        ReuseableChecker reuseableChecker = new ReuseableChecker(tilesPerRow, tilesPerColumn, reuseDistance);
        int[] returnValue = new int[tilesPerRow * tilesPerColumn];
        Arrays.fill(returnValue, -1);

        while (indices.size() > 0) {
            Integer index = indices.remove(0);
            List<Integer> idList = destinationTileIDs.get(index);
            for (Integer id : idList) {
                if(isUsedLessThenMaxReuses(id, returnValue, maxReuses) && reuseableChecker.isReuseableAtPosition(id, returnValue, index))
                {
                    returnValue[index] = id;
                    break;
                }
            }
        }

        return returnValue;
    }

    private boolean isUsedLessThenMaxReuses(int id, int[] imageIds, int maxReuses)
    {
        return Arrays.stream(imageIds).parallel().filter(imageID -> imageID == id).count() <= maxReuses;
    }
}
