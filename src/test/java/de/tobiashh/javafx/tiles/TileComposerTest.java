package de.tobiashh.javafx.tiles;

import de.tobiashh.javafx.tools.ImageTools;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

class TileComposerTest {

    private static Stream<Arguments> testData() {
        return Stream.of(
            Arguments.of(Color.RED, Color.BLUE, 0, 0, new Color(255, 0, 0)),
            Arguments.of(Color.RED, Color.BLUE, 100, 0, new Color(0, 0, 255)),
            Arguments.of(Color.RED, Color.BLUE, 75, 0, new Color(64, 0, 191)),
            Arguments.of(Color.RED, Color.BLUE, 50, 0, new Color(128, 0, 127)),
            Arguments.of(Color.RED, Color.BLUE, 25, 0, new Color(192, 0, 63)),

            Arguments.of(Color.RED, Color.GREEN, 0, 0, new Color(255, 0, 0)),
            Arguments.of(Color.RED, Color.GREEN, 100, 0, new Color(0, 255, 0)),
            Arguments.of(Color.RED, Color.GREEN, 75, 0, new Color(64, 191, 0)),
            Arguments.of(Color.RED, Color.GREEN, 50, 0, new Color(128, 127, 0)),
            Arguments.of(Color.RED, Color.GREEN, 25, 0, new Color(192, 63, 0)),

            Arguments.of(Color.RED, Color.BLUE, 100, 100, new Color(255, 0, 0)),
            Arguments.of(Color.RED, Color.BLUE, 100, 50, new Color(127, 0, 128)),

            Arguments.of(Color.RED, Color.GREEN, 100, 100, new Color(255, 0, 0)),
            Arguments.of(Color.RED, Color.GREEN, 100, 50, new Color(127, 128, 0))
        );
    }

    @ParameterizedTest
    @MethodSource("testData")
    void test(Color originalColor, Color mosaikColor, int opacity, int postColorAlignment, Color resultColor) {
        BufferedImage original = createImage(originalColor);
        BufferedImage mosaik = createImage(mosaikColor);
        BufferedImage result = createImage(resultColor);

        BufferedImage composedImage = new TileComposer(opacity, postColorAlignment).compose(original, mosaik);

        assertThat(composedImage.getRGB(0, 0), is(result.getRGB(0, 0)));
    }

    private static Stream<Arguments> testData2() {
        return Stream.of(
                Arguments.of(0,0),
                Arguments.of(100,0),
                Arguments.of(0,100),
                Arguments.of(100,100)
        );
    }

    @ParameterizedTest
    @MethodSource("testData2")
    void test2(int opacity, int postColorAlignment) {
        BufferedImage original = createImage(Color.BLUE);
        BufferedImage mosaik = createImage(Color.RED);

        BufferedImage composedImage = new TileComposer(opacity, postColorAlignment).compose(original, mosaik);

        assertThat(composedImage, is(not(original)));
        assertThat(composedImage, is(not(mosaik)));
    }

    private BufferedImage createImage(Color color) {
        int width = 1;
        int height = 1;
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = (Graphics2D) bufferedImage.getGraphics();
        graphics2D.setColor(color);
        graphics2D.fillRect(0, 0, width, height);
        return bufferedImage;
    }
}