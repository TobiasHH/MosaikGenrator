package de.tobiashh.javafx;

import de.tobiashh.javafx.tiles.MosaikTile;
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

public class MosaikTilesLoaderTask extends Task<List<MosaikTile>> {
    private static final int MAX_THREADS = Math.max(1,Runtime.getRuntime().availableProcessors() - 1);

    ExecutorService executor = Executors.newFixedThreadPool(MAX_THREADS, runnable -> {
        Thread t = new Thread(runnable);
        t.setDaemon(true);
        return t;
    });

    private final Path newPath;
    private final boolean scanSubFolder;
    private final int tileSize;

    public MosaikTilesLoaderTask(Path newPath, boolean scanSubFolder, int tileSize) {
        this.newPath = newPath;
        this.scanSubFolder = scanSubFolder;
        this.tileSize = tileSize;
    }

    @Override
    protected List<MosaikTile> call() {
        List<MosaikTile> tiles = new ArrayList<>();

        try {
            List<Future<Optional<MosaikTile>>> futures = ((scanSubFolder) ? Files.walk(newPath) : Files.list(newPath))
                    .filter(this::extentionFilter)
                    .map(file -> executor.submit(new MosaikTileLoadTask(file, tileSize)))
                    .collect(Collectors.toList());

            for (Future<Optional<MosaikTile>> future : futures) {
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

    private boolean extentionFilter(Path path) {
        return Arrays
                .stream(MosaikImageModelImpl.FILE_EXTENSION)
                .anyMatch(e -> path.toString().toLowerCase().endsWith(".".concat(e.toLowerCase())));
    }

    private boolean notExtentionFilter(Path path) {
        return Arrays
                .stream(MosaikImageModelImpl.FILE_EXTENSION)
                .noneMatch(e -> path.toString().toLowerCase().endsWith(".".concat(e.toLowerCase())));
    }

}