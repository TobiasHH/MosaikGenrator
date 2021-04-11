package de.tobiashh.javafx;

import de.tobiashh.javafx.tiles.DstTile;
import javafx.concurrent.Task;

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
import java.util.stream.Collectors;

public class DstTilesLoaderTask extends Task<List<DstTile>> {
    private static final int MAX_THREADS = Math.max(1,Runtime.getRuntime().availableProcessors() - 1);

    private final ExecutorService executor = Executors.newFixedThreadPool(MAX_THREADS, runnable -> {
        Thread t = new Thread(runnable);
        t.setDaemon(true);
        return t;
    });

    private final Path newPath;
    private final boolean scanSubFolder;
    private final int tileSize;
    private final int compareSize;

    public DstTilesLoaderTask(Path newPath, boolean scanSubFolder, int tileSize, int compareSize) {
        this.newPath = newPath;
        this.scanSubFolder = scanSubFolder;
        this.tileSize = tileSize;
        this.compareSize = compareSize;
    }

    @Override
    protected List<DstTile> call() {
        List<DstTile> tiles = new ArrayList<>();

        try {
            List<Future<Optional<DstTile>>> futures = ((scanSubFolder) ? Files.walk(newPath) : Files.list(newPath))
                    .filter(this::extensionFilter)
                    .map(file -> executor.submit(new DstTileLoadTask(file, tileSize, compareSize)))
                    .collect(Collectors.toList());

            for (Future<Optional<DstTile>> future : futures) {
                try {
                    future.get().ifPresent(tiles::add);
                    updateProgress(tiles.size(), futures.size());
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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