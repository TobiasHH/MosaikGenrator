package de.tobiashh.javafx;

import de.tobiashh.javafx.model.MosaicImageModelImpl;
import de.tobiashh.javafx.tiles.DstTile;
import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Stream;

public class DstTilesLoaderTask extends Task<List<DstTile>> {
    private final static Logger LOGGER = LoggerFactory.getLogger(DstTilesLoaderTask.class.getName());

    private static final int MAX_THREADS = Math.max(1, Runtime.getRuntime().availableProcessors() - 1);

    private final ExecutorService executor = Executors.newFixedThreadPool(MAX_THREADS, runnable -> {
        Thread t = new Thread(runnable);
        t.setDaemon(true);
        return t;
    });

    private final Path tilesPath;
    private final Path cachePath;
    private final boolean scanSubFolder;
    private final int tileSize;
    private final int compareSize;

    public DstTilesLoaderTask(Path tilesPath, Path cachePath, boolean scanSubFolder, int tileSize, int compareSize) {
        LOGGER.info("DstTilesLoaderTask");
        this.tilesPath = tilesPath;
        this.cachePath = cachePath;
        this.scanSubFolder = scanSubFolder;
        this.tileSize = tileSize;
        this.compareSize = compareSize;
    }

    @Override
    protected List<DstTile> call() {
        LOGGER.info("loading Tiles from {}", tilesPath);

        List<DstTile> tiles = new ArrayList<>();

        try (Stream<Path> pathStream = scanSubFolder ? Files.walk(tilesPath) : Files.list(tilesPath)) {
            List<Future<Optional<DstTile>>> futures = pathStream.filter(this::extensionFilter)
                    .map(file -> executor.submit(new DstTileLoadTask(file, cachePath, tileSize, compareSize)))
                    .toList();

            for (Future<Optional<DstTile>> future : futures) {
                try {
                    future.get().ifPresent(tiles::add);
                    updateProgress(tiles.size(), futures.size());
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        LOGGER.info("Tiles loaded");
        return tiles;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        executor.shutdownNow();
        return super.cancel(mayInterruptIfRunning);
    }

    private boolean extensionFilter(Path path) {
        return Arrays
                .stream(MosaicImageModelImpl.FILE_EXTENSION)
                .anyMatch(e -> path.toString().toLowerCase().endsWith(".".concat(e.toLowerCase())));
    }
}