package de.tobiashh.javafx.composer;

import de.tobiashh.javafx.TilesStraightDistance;

import java.util.stream.IntStream;

public class ReuseableChecker {
    private final int tilesPerRow;
    private final int tilesPerColumn;
    private final int reuseDistance;
    private final TilesStraightDistance tilesStraightDistance;

    public ReuseableChecker(int tilesPerRow, int tilesPerColumn, int reuseDistance) {
        this.tilesPerRow = tilesPerRow;
        this.tilesPerColumn = tilesPerColumn;
        this.reuseDistance = reuseDistance;
        this.tilesStraightDistance = new TilesStraightDistance(tilesPerRow);
    }

    public boolean isReuseableAtPosition(Integer destinationTileID, int[] destinationTileIDs, int index) {
        return IntStream.range(0, tilesPerRow * tilesPerColumn).parallel().allMatch(i -> {
            if( i == index ) return true;
            if (destinationTileIDs[i] != destinationTileID) return true;
            return tilesStraightDistance.calculate(i, index) >= reuseDistance;
        });
    }
}