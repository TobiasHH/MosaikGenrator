package de.tobiashh.javafx;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class TilesCircularDistanceTest {

    @Test
    void getTileDistance() {

        TilesCircularDistance tileDistance = new TilesCircularDistance(5);
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
        assertThat(tileDistance.calculate(1,7), is(1));
        assertThat(tileDistance.calculate(7,1), is(1));

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
         * XX2XX
         */
        assertThat(tileDistance.calculate(1,12), is(2));
        assertThat(tileDistance.calculate(12,1), is(2));

        /*
         * X1XXX
         * XXXXX
         * XXX2X
         */
        assertThat(tileDistance.calculate(1,13), is(2));
        assertThat(tileDistance.calculate(13,1), is(2));

        /*
         * X1X2X
         * XXXXX
         * XXXXX
         */
        assertThat(tileDistance.calculate(1,3), is(2));
        assertThat(tileDistance.calculate(3,1), is(2));

        /*
         * X1XXX
         * XXXXX
         * XXXX2
         */
        assertThat(tileDistance.calculate(1,14), is(3));
        assertThat(tileDistance.calculate(14,1), is(3));


        /*
         * 1XXXX
         * XXXXX
         * XXXX2
         */
        assertThat(tileDistance.calculate(0,14), is(4));
        assertThat(tileDistance.calculate(14,0), is(4));



        /*
         * 1XXXX
         * XXXXX
         * XXXXX
         * XXXXX
         * XXXX2
         */
        assertThat(tileDistance.calculate(0,24), is(5));
        assertThat(tileDistance.calculate(24,0), is(5));
    }
}