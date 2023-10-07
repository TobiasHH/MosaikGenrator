package de.tobiashh.javafx.tools;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ImageToolsTest {

    @Test
    void red() {
        assertEquals(255, ImageTools.red(0x00FF0000));
    }

    @Test
    void green() {
        assertEquals(255, ImageTools.green(0x0000FF00));
    }

    @Test
    void blue() {
        assertEquals(255, ImageTools.blue(0x000000FF));
    }
}