package de.tobiashh.javafx.model;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class MosaicImageModelImplTest {

    @Test
    void getIndexSortedByScore() {
        MosaicImageModelImpl model = new MosaicImageModelImpl();
        Map<Integer, Integer> scores = new HashMap<>();
        scores.put(0, 5);
        scores.put(1, 1);
        scores.put(2, 5);
        scores.put(3, 2);


        List<Integer> keys = model.getIndexSortedByScore(scores);
        assertThat(keys.get(0), is(1));
        assertThat(keys.get(1), is(3));
        assertThat(keys.get(2), is(0));
        assertThat(keys.get(3), is(2));
    }
}