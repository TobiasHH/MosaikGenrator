package de.tobiashh.javafx.model;

import de.tobiashh.javafx.tools.Index2D;
import de.tobiashh.javafx.tools.IndexConverter;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class IndexConverterTest {

    @Test
    void convertLinearTo2D() {
        IndexConverter indexConverter = new IndexConverter(5);
        assertAll(
            () -> assertThat(indexConverter.convertLinearTo2D(0), equalTo(new Index2D(0,0))),
            () -> assertThat(indexConverter.convertLinearTo2D(4), equalTo(new Index2D(4,0))),
            () -> assertThat(indexConverter.convertLinearTo2D(5), equalTo(new Index2D(0,1))),
            () -> assertThat(indexConverter.convertLinearTo2D(9), equalTo(new Index2D(4,1))),
            () -> assertThat(indexConverter.convertLinearTo2D(10), equalTo(new Index2D(0,2)))
        );
    }

    @Test
    void convert2DToLinear() {
        IndexConverter indexConverter = new IndexConverter(5);
        assertAll(
            () -> assertThat(indexConverter.convert2DToLinear(new Index2D(0,0)), equalTo(0)),
            () -> assertThat(indexConverter.convert2DToLinear(new Index2D(4,0)), equalTo(4)),
            () -> assertThat(indexConverter.convert2DToLinear(new Index2D(0,1)), equalTo(5)),
            () -> assertThat(indexConverter.convert2DToLinear(new Index2D(4,1)), equalTo(9)),
            () -> assertThat(indexConverter.convert2DToLinear(new Index2D(0,2)), equalTo(10))
        );
    }
}