package de.tobiashh.javafx;

import de.tobiashh.javafx.model.MosaicImageModel;
import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;

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
