package de.tobiashh.javafx.compareable;

import de.tobiashh.javafx.tools.ImageTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;

public abstract class ComparableImage {
    private final static Logger LOGGER = LoggerFactory.getLogger(ComparableImage.class.getName());

    protected BufferedImage compareImage;

    protected void setDataImage(BufferedImage dataImage, int compareSize) {
        LOGGER.trace("setDataImage");
        this.compareImage = ImageTools.calculateScaledImage(dataImage, compareSize, true);
    }

    public abstract int compare(ComparableImage ci, int colorAlignment);
}
