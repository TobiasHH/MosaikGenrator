package de.tobiashh.javafx.model;

import de.tobiashh.javafx.Mode;
import de.tobiashh.javafx.composer.ImageComposerFactory;
import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class MosaikImageGenerateTask extends Task<List<Integer>> {
    private final static Logger LOGGER = LoggerFactory.getLogger(MosaikImageGenerateTask.class.getName());

    private final List<List<Integer>> scoredDstTileLists;
    private final Mode mode;
    private final int tilesPerRow;
    private final int tilesPerColumn;
    private final int maxReuses;
    private final int reuseDistance;
    private final List<Integer> areaOfInterest;

    public MosaikImageGenerateTask(
            MosaicImageModelImpl mosaicImageModel,
            List<List<Integer>> scoredDstTileLists,
            List<Integer> areaOfInterest
    ) {
        this.scoredDstTileLists = scoredDstTileLists;
        this.mode = mosaicImageModel.modeProperty().get();
        this.tilesPerRow = mosaicImageModel.tilesPerRowProperty().get();
        this.tilesPerColumn = mosaicImageModel.tilesPerColumnProperty().get();
        this.maxReuses = mosaicImageModel.maxReusesProperty().get();
        this.reuseDistance = mosaicImageModel.reuseDistanceProperty().get();
        this.areaOfInterest = areaOfInterest;
    }

    @Override
    protected List<Integer> call() {
        LOGGER.info("start");

        LOGGER.info("generate image start");
        List<Integer> destinationTileIndexes = ImageComposerFactory
                .getComposer(mode)
                .generate(tilesPerRow
                        , tilesPerColumn
                        , maxReuses
                        , reuseDistance
                        , areaOfInterest
                        , scoredDstTileLists);
        LOGGER.info("finished");
        return destinationTileIndexes;
    }
}
