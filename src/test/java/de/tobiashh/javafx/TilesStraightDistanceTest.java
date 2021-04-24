package de.tobiashh.javafx;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

class TilesStraightDistanceTest {

    @Test
    void getTileDistance() {
        TilesStraightDistance tileDistance = new TilesStraightDistance(5);
        /*
         * X1XXX
         * X2XXX
         */
        assertThat(tileDistance.calculate(1,6), is(1));
        assertThat(tileDistance.calculate(6,1), is(1));

        /*
         * X1XXX
         * XX2XX
         */
        assertThat(tileDistance.calculate(1,7), is(2));
        assertThat(tileDistance.calculate(7,1), is(2));

        /*
         * X12XX
         * XXXXX
         */
        assertThat(tileDistance.calculate(1,2), is(1));
        assertThat(tileDistance.calculate(2,1), is(1));

        /*
         * X1XXX
         * XXXXX
         * X2XXX
         */
        assertThat(tileDistance.calculate(1,11), is(2));
        assertThat(tileDistance.calculate(11,1), is(2));

        /*
         * X1XXX
         * XXXXX
         * XXX2X
         */
        assertThat(tileDistance.calculate(1,13), is(4));
        assertThat(tileDistance.calculate(13,1), is(4));

        /*
         * X1X2X
         * XXXXX
         * XXXXX
         */
        assertThat(tileDistance.calculate(1,3), is(2));
        assertThat(tileDistance.calculate(3,1), is(2));
    }
}