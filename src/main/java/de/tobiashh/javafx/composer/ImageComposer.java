package de.tobiashh.javafx.composer;

import java.util.List;

public interface ImageComposer {
    List<Integer> generate(int tilesPerRow, int tilesPerColumn, int maxReuses, int reuseDistance, List<Integer> areaOfInterest, List<List<Integer>> destinationTileIDs);
}
