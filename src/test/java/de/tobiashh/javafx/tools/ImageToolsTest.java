package de.tobiashh.javafx.tools;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ImageToolsTest {

    @Test
    void redTest() {
        assertEquals(255, ImageTools.red(0x00FF0000));
    }

    @Test
    void greenTest() {
        assertEquals(255, ImageTools.green(0x0000FF00));
    }

    @Test
    void blueTest() {
        assertEquals(255, ImageTools.blue(0x000000FF));
    }

    @Test
    void rgbTest() {
        for (int color : new int[] {0x00FFFFFF, 0x00FF0000, 0x0000FF00, 0x000000FF, 0x00998877, 0x00000000}) {
            int color2 = ImageTools.rgb(ImageTools.red(color), ImageTools.green(color), ImageTools.blue(color));
            assertEquals(color, color2);
        }
    }
}