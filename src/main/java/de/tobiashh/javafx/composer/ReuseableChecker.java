package de.tobiashh.javafx.composer;

import distanceCalculator.StraightTileDistanceCalculator;

import java.util.stream.IntStream;

public class ReuseableChecker {
    private final int tilesPerRow;
    private final int tilesPerColumn;
    private final int reuseDistance;
    private final StraightTileDistanceCalculator straightTileDistanceCalculator;

    public ReuseableChecker(int tilesPerRow, int tilesPerColumn, int reuseDistance) {
        this.tilesPerRow = tilesPerRow;
        this.tilesPerColumn = tilesPerColumn;
        this.reuseDistance = reuseDistance;
        this.straightTileDistanceCalculator = new StraightTileDistanceCalculator(tilesPerRow);
    }

    public boolean isReuseableAtPosition(Integer destinationTileID, Integer[] destinationTileIDs, int tileIndex) {
        return IntStream.range(0, tilesPerRow * tilesPerColumn).allMatch(index -> check(destinationTileID, destinationTileIDs[index], tileIndex, index));
    }

    private boolean check(Integer destinationTileID1, Integer destinationTileID2, int index1, int index2) {
        if( index1 == index2) return true;
        if (!destinationTileID1.equals(destinationTileID2)) return true;
        return straightTileDistanceCalculator.calculate(index1, index2) >= reuseDistance;
    }
}
