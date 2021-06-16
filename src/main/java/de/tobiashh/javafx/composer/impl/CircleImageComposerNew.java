package de.tobiashh.javafx.composer.impl;

import de.tobiashh.javafx.TilesCircularDistance;
import de.tobiashh.javafx.TilesStraightDistance;
import de.tobiashh.javafx.composer.ImageComposer;
import de.tobiashh.javafx.tools.Index2D;
import de.tobiashh.javafx.tools.IndexConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CircleImageComposerNew implements ImageComposer {
    private final static Logger LOGGER = LoggerFactory.getLogger(CircleImageComposerNew.class.getName());

    private int tilesPerRow;
    private int tilesPerColumn;
    private int maxReuses;
    private int reuseDistance;

    @Override
    public List<Integer> generate(int tilesPerRow, int tilesPerColumn, int maxReuses, int reuseDistance, List<Integer> areaOfInterest, List<List<Integer>> destinationTileIDs) {
        this.tilesPerRow = tilesPerRow;
        this.tilesPerColumn = tilesPerColumn;
        this.maxReuses = maxReuses;
        this.reuseDistance = reuseDistance;
        LOGGER.info("generateLinearImage");

        int[] returnValue = new int[tilesPerRow * tilesPerColumn];
        Arrays.fill(returnValue, -1);

        List<Integer> tileIndices = IntStream.range(0, tilesPerColumn * tilesPerRow).boxed().collect(Collectors.toList());
        List<Integer> areaOfInterestIndices = new ArrayList<>(areaOfInterest);
        tileIndices.removeAll(areaOfInterestIndices);

        IndexConverter indexConverter = new IndexConverter(tilesPerRow);
        TilesCircularDistance tilesCircularDistance = new TilesCircularDistance(tilesPerRow);
        int startIndex = indexConverter.convert2DToLinear(new Index2D(tilesPerRow / 2, tilesPerColumn / 2));
        tileIndices = sort(tileIndices, tilesCircularDistance, startIndex);
        areaOfInterestIndices = sort(areaOfInterestIndices, tilesCircularDistance, startIndex);

        while (areaOfInterestIndices.size() > 0) {
            Integer index = areaOfInterestIndices.remove(0);
            List<Integer> idList = destinationTileIDs.get(index);
            for (Integer id : idList) {
                if(isUsedLessThenMaxReuses(id, returnValue) && isReuseableAtPosition(id, returnValue, index))
                {
                    returnValue[index] = id;
                    break;
                }
            }
        }

        while (tileIndices.size() > 0) {
            Integer index = tileIndices.remove(0);
            List<Integer> idList = destinationTileIDs.get(index);
            for (Integer id : idList) {
                if(isUsedLessThenMaxReuses(id, returnValue) && isReuseableAtPosition(id, returnValue, index))
                {
                    returnValue[index] = id;
                    break;
                }
            }
        }


        return Arrays.stream(returnValue).boxed().collect(Collectors.toList());
    }

    private boolean isReuseableAtPosition(Integer id, int[] returnValue, int index) {
        TilesStraightDistance tilesStraightDistance = new TilesStraightDistance(tilesPerRow);
        return IntStream.range(0, tilesPerRow * tilesPerColumn).noneMatch(i -> {
            boolean notSamePosition = i != index;
            boolean insideReuseDistance = tilesStraightDistance.calculate(i, index) < reuseDistance;
            boolean sameID = returnValue[i] == id;
            return insideReuseDistance && notSamePosition && sameID;
        });
    }

    private boolean isUsedLessThenMaxReuses(int id, int[] imageIds)
    {
        return Arrays.stream(imageIds).filter(imageID -> imageID == id).count() <= maxReuses;
    }

    private List<Integer> sort(List<Integer> list, TilesCircularDistance tilesCircularDistance, int startIndex) {
        return list.stream().sorted(Comparator.comparingInt(value -> tilesCircularDistance.calculate(value, startIndex))).collect(Collectors.toList());
    }
}
