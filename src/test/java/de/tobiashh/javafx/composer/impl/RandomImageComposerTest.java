package de.tobiashh.javafx.composer.impl;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class RandomImageComposerTest {

    @Test
    void generate() {
        RandomImageComposer randomImageComposer = new RandomImageComposer();
        int tilesPerRow = 1;
        int tilesPerColumn = 1;
        int reuseDistance = 1;
        int maxReuses = 0;
        List<Integer> areaOfInterest = new ArrayList<>();

        List<List<Integer>> destinationTileIDs = new ArrayList<>();
        destinationTileIDs.add(new ArrayList<>(Arrays.asList(2, 1, 0)));

        List<Integer> result = randomImageComposer.generate(tilesPerRow,
                tilesPerColumn,
                maxReuses,
                reuseDistance,
                areaOfInterest,
                destinationTileIDs);

        assertThat(result, is(Collections.singletonList(2)));
    }

    @Test
    void generate2() {
        RandomImageComposer randomImageComposer = new RandomImageComposer();
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

        List<Integer> result = randomImageComposer.generate(tilesPerRow,
                tilesPerColumn,
                maxReuses,
                reuseDistance,
                areaOfInterest,
                destinationTileIDs);

        assertThat(result.stream().filter(value -> value == 2).count(), is(1L));
        assertThat(result.stream().filter(value -> value == 1).count(), is(1L));
        assertThat(result.stream().filter(value -> value == 0).count(), is(1L));
        assertThat(result.stream().filter(value -> value == -1).count(), is(6L));
    }

    @RepeatedTest(value = 100, name = "Wiederholungs Lauf {currentRepetition} von {totalRepetitions}")
    void generate3() {
        RandomImageComposer randomImageComposer = new RandomImageComposer();
        int tilesPerRow = 3;
        int tilesPerColumn = 3;
        int reuseDistance = 1;
        int maxReuses = 0;
        List<Integer> areaOfInterest = new ArrayList<>(Collections.singletonList(6));

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

        List<Integer> result = randomImageComposer.generate(tilesPerRow,
                tilesPerColumn,
                maxReuses,
                reuseDistance,
                areaOfInterest,
                destinationTileIDs);

        assertThat(result.get(6), is(1));

        assertThat(result.stream().filter(value -> value == 2).count(), is(1L));
        assertThat(result.stream().filter(value -> value == 1).count(), is(1L));
        assertThat(result.stream().filter(value -> value == 0).count(), is(1L));
        assertThat(result.stream().filter(value -> value == -1).count(), is(6L));
    }
}