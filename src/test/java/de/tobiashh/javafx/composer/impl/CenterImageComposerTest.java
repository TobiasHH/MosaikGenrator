package de.tobiashh.javafx.composer.impl;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class CenterImageComposerTest {

    @Test
    void generate() {
        CenterImageComposer centerImageComposer = new CenterImageComposer();
        int tilesPerRow = 1;
        int tilesPerColumn = 1;
        int reuseDistance = 1;
        int maxReuses = 0;
        List<Integer> areaOfInterest = new ArrayList<>();

        List<List<Integer>> destinationTileIDs = new ArrayList<>();
        destinationTileIDs.add(new ArrayList<>(Arrays.asList(2, 1, 0)));

        List<Integer> result = centerImageComposer.generate(tilesPerRow,
                tilesPerColumn,
                maxReuses,
                reuseDistance,
                areaOfInterest,
                destinationTileIDs);

        assertThat(result, is(Collections.singletonList(2)));
    }
    @Test
    void generate2() {
        CenterImageComposer centerImageComposer = new CenterImageComposer();
        int tilesPerRow = 3;
        int tilesPerColumn = 3;
        int reuseDistance = 1;
        int maxReuses = 0;
        List<Integer> areaOfInterest = new ArrayList<>();

        List<List<Integer>> destinationTileIDs = new ArrayList<>();
        destinationTileIDs.add(new ArrayList<>(Arrays.asList( 1, 0, 2)));
        destinationTileIDs.add(new ArrayList<>(Arrays.asList( 2, 0, 1)));
        destinationTileIDs.add(new ArrayList<>(Arrays.asList( 1, 0, 2)));
        destinationTileIDs.add(new ArrayList<>(Arrays.asList( 1, 0, 2)));
        destinationTileIDs.add(new ArrayList<>(Arrays.asList( 1, 0, 2)));
        destinationTileIDs.add(new ArrayList<>(Arrays.asList( 1, 0, 2)));
        destinationTileIDs.add(new ArrayList<>(Arrays.asList( 1, 0, 2)));
        destinationTileIDs.add(new ArrayList<>(Arrays.asList( 1, 0, 2)));
        destinationTileIDs.add(new ArrayList<>(Arrays.asList( 1, 0, 2)));

        List<Integer> result = centerImageComposer.generate(tilesPerRow,
                tilesPerColumn,
                maxReuses,
                reuseDistance,
                areaOfInterest,
                destinationTileIDs);

        assertThat(result, is(Arrays.asList(-1, 2, -1, 0, 1, -1, -1, -1, -1)));
    }
}