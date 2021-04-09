package de.tobiashh.javafx;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class MosaicImageModelImplTest {

    @Test
    void getDistance() {
        MosaicImageModelImpl model = new MosaicImageModelImpl();
        model.setTilesPerRow(5);
        /*
         * X1XXX
         * X2XXX
         */
        assertThat(model.getDistance(1,6), is(1));
        assertThat(model.getDistance(6,1), is(1));

        /*
         * X1XXX
         * XX2XX
         */
        assertThat(model.getDistance(1,7), is(2));
        assertThat(model.getDistance(7,1), is(2));

        /*
         * X12XX
         * XXXXX
         */
        assertThat(model.getDistance(1,2), is(1));
        assertThat(model.getDistance(2,1), is(1));

        /*
         * X1XXX
         * XXXXX
         * X2XXX
         */
        assertThat(model.getDistance(1,11), is(2));
        assertThat(model.getDistance(11,1), is(2));

        /*
         * X1XXX
         * XXXXX
         * XXX2X
         */
        assertThat(model.getDistance(1,13), is(4));
        assertThat(model.getDistance(13,1), is(4));

        /*
         * X1X2X
         * XXXXX
         * XXXXX
         */
        assertThat(model.getDistance(1,3), is(2));
        assertThat(model.getDistance(3,1), is(2));
    }
}