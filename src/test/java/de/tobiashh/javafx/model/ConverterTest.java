package de.tobiashh.javafx.model;

import de.tobiashh.javafx.tools.Converter;
import de.tobiashh.javafx.tools.Position;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class ConverterTest {

    @Test
    void convertLinearTo2D() {
        Converter converter = new Converter(5);
        assertAll(
                () -> assertThat(converter.getPosition(0), equalTo(new Position(0, 0))),
                () -> assertThat(converter.getPosition(4), equalTo(new Position(4, 0))),
                () -> assertThat(converter.getPosition(5), equalTo(new Position(0, 1))),
                () -> assertThat(converter.getPosition(9), equalTo(new Position(4, 1))),
                () -> assertThat(converter.getPosition(10), equalTo(new Position(0, 2)))
        );
    }

    @Test
    void convert2DToLinear() {
        Converter converter = new Converter(5);
        assertAll(
                () -> assertThat(converter.getIndex(new Position(0, 0)), equalTo(0)),
                () -> assertThat(converter.getIndex(new Position(4, 0)), equalTo(4)),
                () -> assertThat(converter.getIndex(new Position(0, 1)), equalTo(5)),
                () -> assertThat(converter.getIndex(new Position(4, 1)), equalTo(9)),
                () -> assertThat(converter.getIndex(new Position(0, 2)), equalTo(10))
        );
    }
}