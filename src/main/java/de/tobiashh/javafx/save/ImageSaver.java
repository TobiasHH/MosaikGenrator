package de.tobiashh.javafx.save;

import de.tobiashh.javafx.Controller;
import de.tobiashh.javafx.tiles.OriginalTile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;

public class ImageSaver implements Runnable {
    private final static Logger LOGGER = LoggerFactory.getLogger(ImageSaver.class.getName());

    private final Controller controller;
    private final Path file;
    private final OriginalTile[] tiles;
    private final int tileSize;
    private final int tilesPerRow;
    private final int tilesPerColumn;

    public ImageSaver(Path file, OriginalTile[] tiles, int tilesPerRow, int tilesPerColumn, int tileSize, Controller controller) {
        LOGGER.info("ImageSaver");
        this.tiles = tiles;
        this.tileSize = tileSize;
        this.tilesPerRow = tilesPerRow;
        this.tilesPerColumn = tilesPerColumn;
        this.file = file;
        this.controller = controller;
    }

    @Override
    public void run() {
        LOGGER.info("Save image {}", file.getFileName());
        Runtime.getRuntime().gc();

        try {

            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("png");
            if (writers.hasNext()) {

                ImageWriter writer = writers.next();

                ImageOutputStream ios = ImageIO.createImageOutputStream(file.toFile());
                writer.setOutput(ios);

                TileRenderImage tm = new TileRenderImage(tilesPerRow, tilesPerColumn, tileSize, tiles);
                writer.write(tm);
                writer.dispose();
                ios.close();
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        controller.setStatus("Image saved");
        LOGGER.info("Image saved");
    }

}
