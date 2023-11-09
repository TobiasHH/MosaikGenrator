package de.tobiashh.javafx.composer;

import de.tobiashh.javafx.Mode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ImageComposerFactoryTest {

    @Test
    void getCircleImageComposerNew() {
        ImageComposer imageComposer = ImageComposerFactory.getComposer(Mode.CIRCULAR_NEW);
        assertTrue(imageComposer instanceof CircleImageComposer);
    }

    @Test
    void getCentermageComposerNew() {
        ImageComposer imageComposer = ImageComposerFactory.getComposer(Mode.TILE_DISTANCE_NEW);
        assertTrue(imageComposer instanceof CenterImageComposer);
    }

    @Test
    void getRandomImageComposerNew() {
        ImageComposer imageComposer = ImageComposerFactory.getComposer(Mode.RANDOM_NEW);
        assertTrue(imageComposer instanceof RandomImageComposer);
    }

    @Test
    void getLinearImageComposerNew() {
        ImageComposer imageComposer = ImageComposerFactory.getComposer(Mode.LINEAR_NEW);
        assertTrue(imageComposer instanceof LinearImageComposer);
    }
}