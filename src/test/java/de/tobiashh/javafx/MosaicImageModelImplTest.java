package de.tobiashh.javafx;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class MosaicImageModelImplTest {

    @Test
    void getDistance() {
        MosaicImageModelImpl model = new MosaicImageModelImpl();
        model.setTilesPerRow(5);
        /*
         * X1XXX
         * X2XXX
         */
        assertThat(model.getTileDistance(1,6), is(1));
        assertThat(model.getTileDistance(6,1), is(1));

        /*
         * X1XXX
         * XX2XX
         */
        assertThat(model.getTileDistance(1,7), is(2));
        assertThat(model.getTileDistance(7,1), is(2));

        /*
         * X12XX
         * XXXXX
         */
        assertThat(model.getTileDistance(1,2), is(1));
        assertThat(model.getTileDistance(2,1), is(1));

        /*
         * X1XXX
         * XXXXX
         * X2XXX
         */
        assertThat(model.getTileDistance(1,11), is(2));
        assertThat(model.getTileDistance(11,1), is(2));

        /*
         * X1XXX
         * XXXXX
         * XXX2X
         */
        assertThat(model.getTileDistance(1,13), is(4));
        assertThat(model.getTileDistance(13,1), is(4));

        /*
         * X1X2X
         * XXXXX
         * XXXXX
         */
        assertThat(model.getTileDistance(1,3), is(2));
        assertThat(model.getTileDistance(3,1), is(2));
    }
}