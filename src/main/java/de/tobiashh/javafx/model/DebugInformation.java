package de.tobiashh.javafx.model;

import java.util.Arrays;

public record DebugInformation(int x, int y, int mosaikArrayIndex, boolean areaOfIntrest, int destinationTileIndex, int reuses, int[] reusePositions, int scoredTileListIndex) {
    public String reusePositionsAsString()
    {
        return Arrays.toString(reusePositions);
    }
}
