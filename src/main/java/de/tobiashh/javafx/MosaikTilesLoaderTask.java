package de.tobiashh.javafx;

import de.tobiashh.javafx.tiles.MosaikTile;
import de.tobiashh.javafx.tiles.Tile;
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
import java.util.stream.Stream;

public class MosaikTilesLoaderTask extends Task<List<MosaikTile>> {
    private static final int MAX_THREADS = Math.max(1,Runtime.getRuntime().availableProcessors() - 1);

    ExecutorService executor = Executors.newFixedThreadPool(MAX_THREADS, runnable -> {
        Thread t = new Thread(runnable);
        t.setDaemon(true);
        return t;
    });

    private final Path newPath;
    private final boolean scanSubFolder;

    public MosaikTilesLoaderTask(Path newPath, boolean scanSubFolder) {
        this.newPath = newPath;
        this.scanSubFolder = scanSubFolder;
    }

    @Override
    protected List<MosaikTile> call() throws Exception {
        List<MosaikTile> tiles = new ArrayList<>();

        try {
            List<Future<Optional<MosaikTile>>> futures = ((scanSubFolder) ? Files.walk(newPath) : Files.list(newPath))
                    .filter(this::extentionFilter)
                    .map(file -> executor.submit(new MosaikTileLoadTask(file)))
                    .collect(Collectors.toList());

            //for debug
            System.out.println("image count = " + ((scanSubFolder) ? Files.walk(newPath) : Files.list(newPath))
                    .filter(this::extentionFilter).count());

            System.out.println("other count = " + ((scanSubFolder) ? Files.walk(newPath) : Files.list(newPath))
                    .filter(this::notExtentionFilter).count());

            ((scanSubFolder) ? Files.walk(newPath) : Files.list(newPath))
                    .filter(this::notExtentionFilter).forEach(path -> {
                        System.out.print("not an image = " + path);
                try {
                    System.out.println(" " + Files.isHidden(path));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });


            for (Future<Optional<MosaikTile>> future : futures) {
                try {
                    future.get().ifPresent(tile -> tiles.add(tile));
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