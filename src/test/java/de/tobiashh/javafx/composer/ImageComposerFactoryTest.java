package de.tobiashh.javafx.composer;

import de.tobiashh.javafx.Mode;
import de.tobiashh.javafx.composer.impl.CenterImageComposerNew;
import de.tobiashh.javafx.composer.impl.CircleImageComposerNew;
import de.tobiashh.javafx.composer.impl.LinearImageComposerNew;
import de.tobiashh.javafx.composer.impl.RandomImageComposerNew;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ImageComposerFactoryTest {

    @Test
    void getCircleImageComposerNew() {
        ImageComposer imageComposer = ImageComposerFactory.getComposer(Mode.CIRCULAR_NEW);
        assertTrue(imageComposer instanceof CircleImageComposerNew);
    }

    @Test
    void getCentermageComposerNew() {
        ImageComposer imageComposer = ImageComposerFactory.getComposer(Mode.TILE_DISTANCE_NEW);
        assertTrue(imageComposer instanceof CenterImageComposerNew);
    }

    @Test
    void getRandomImageComposerNew() {
        ImageComposer imageComposer = ImageComposerFactory.getComposer(Mode.RANDOM_NEW);
        assertTrue(imageComposer instanceof RandomImageComposerNew);
    }

    @Test
    void getLinearImageComposerNew() {
        ImageComposer imageComposer = ImageComposerFactory.getComposer(Mode.LINEAR_NEW);
        assertTrue(imageComposer instanceof LinearImageComposerNew);
    }
}