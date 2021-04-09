package de.tobiashh.javafx;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class MosaikImageModelImplTest {

    @Test
    void getDistance() {
        MosaikImageModelImpl model = new MosaikImageModelImpl();
        /*
         * X1XXX
         * X2XXX
         */
        assertThat(model.getDistance(1,6,5), is(1));
        assertThat(model.getDistance(6,1,5), is(1));

        /*
         * X1XXX
         * XX2XX
         */
        assertThat(model.getDistance(1,7,5), is(2));
        assertThat(model.getDistance(7,1,5), is(2));

        /*
         * X12XX
         * XXXXX
         */
        assertThat(model.getDistance(1,2,5), is(1));
        assertThat(model.getDistance(2,1,5), is(1));

        /*
         * X1XXX
         * XXXXX
         * X2XXX
         */
        assertThat(model.getDistance(1,11,5), is(2));
        assertThat(model.getDistance(11,1,5), is(2));

        /*
         * X1XXX
         * XXXXX
         * XXX2X
         */
        assertThat(model.getDistance(1,13,5), is(4));
        assertThat(model.getDistance(13,1,5), is(4));

        /*
         * X1X2X
         * XXXXX
         * XXXXX
         */
        assertThat(model.getDistance(1,3,5), is(2));
        assertThat(model.getDistance(3,1,5), is(2));
    }
}