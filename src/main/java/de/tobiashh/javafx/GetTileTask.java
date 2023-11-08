package de.tobiashh.javafx;

import de.tobiashh.javafx.Mode;
import de.tobiashh.javafx.composer.ImageComposerFactory;
import de.tobiashh.javafx.model.MosaicImageModel;
import de.tobiashh.javafx.model.MosaicImageModelImpl;
import de.tobiashh.javafx.tiles.DstTile;
import de.tobiashh.javafx.tiles.OriginalTile;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GetTileTask extends Task<BufferedImage> {
    private final static Logger LOGGER = LoggerFactory.getLogger(GetTileTask.class.getName());

    private final int x;
    private final int y;
    private final boolean displayOriginalImage;
    private final MosaicImageModel model;

    public GetTileTask(MosaicImageModel model, int x, int y, boolean displayOriginalImage) {
        this.x = x;
        this.y = y;
        this.displayOriginalImage = displayOriginalImage;
        this.model = model;
    }

    @Override
    protected BufferedImage call() {
        LOGGER.debug("GetTileTask start");
        BufferedImage retval = model.getTile(x, y, displayOriginalImage);

        LOGGER.debug("GetTileTask finished");
        return retval;
    }
}
