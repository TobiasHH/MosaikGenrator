package de.tobiashh.javafx.model;

import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ImageTilerTest {

    @Test
    void getTiles() {
        BufferedImage testImage = createTestImage(40, 40);
        ImageTiler imageTiler = new ImageTiler(testImage, 15, 3, 2);
        List<BufferedImage> tiles = imageTiler.getTiles();
        assertThat(tiles.size(), is(6));
        assertAll(
                () -> assertTrue(compareImagesPixelwise(tiles.get(0), createTestImage(15, 15))),
                () -> assertTrue(compareImagesPixelwise(tiles.get(1), createTestImage(15, 15))),
                () -> assertTrue(compareImagesPixelwise(tiles.get(2), createTestImage(15, 15))),
                () -> assertTrue(compareImagesPixelwise(tiles.get(3), createTestImage(15, 15))),
                () -> assertTrue(compareImagesPixelwise(tiles.get(4), createTestImage(15, 15))),
                () -> assertTrue(compareImagesPixelwise(tiles.get(5), createTestImage(15, 15)))
        );
    }

    private BufferedImage createTestImage(int width, int height) {
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = (Graphics2D) bufferedImage.getGraphics();
        graphics2D.setColor(Color.RED);
        graphics2D.fillRect(0, 0, width, height);
        return bufferedImage;
    }

    private boolean compareImagesPixelwise(BufferedImage imgA, BufferedImage imgB) {
        if (imgA.getWidth() != imgB.getWidth() || imgA.getHeight() != imgB.getHeight()) {
            return false;
        }

        int width = imgA.getWidth();
        int height = imgA.getHeight();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (imgA.getRGB(x, y) != imgB.getRGB(x, y)) {
                    return false;
                }
            }
        }

        return true;
    }
}