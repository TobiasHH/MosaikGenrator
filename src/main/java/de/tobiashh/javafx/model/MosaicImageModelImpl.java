package de.tobiashh.javafx.model;

import de.tobiashh.javafx.Controller;
import de.tobiashh.javafx.DstTilesLoaderTask;
import de.tobiashh.javafx.Mode;
import de.tobiashh.javafx.save.ImageSaver;
import de.tobiashh.javafx.tiles.DstTile;
import de.tobiashh.javafx.tiles.OriginalTile;
import de.tobiashh.javafx.tools.Converter;
import de.tobiashh.javafx.tools.ImageTools;
import de.tobiashh.javafx.tools.Position;
import distanceCalculator.StraightTileDistanceCalculator;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MosaicImageModelImpl implements MosaicImageModel {
    public static final String[] FILE_EXTENSION = {"png", "jpg", "jpeg"};
    private final static Logger LOGGER = LoggerFactory.getLogger(MosaicImageModelImpl.class.getName());
    private final ReadOnlyIntegerWrapper dstTilesCount = new ReadOnlyIntegerWrapper();
    private final ReadOnlyIntegerWrapper usedCount = new ReadOnlyIntegerWrapper();
    private final ReadOnlyIntegerWrapper tilesMinNeeded = new ReadOnlyIntegerWrapper();
    private final ReadOnlyIntegerWrapper tilesPerColumn = new ReadOnlyIntegerWrapper();
    private final ReadOnlyIntegerWrapper dstTilesLoadProgress = new ReadOnlyIntegerWrapper();
    private final ReadOnlyBooleanWrapper imageCalculated = new ReadOnlyBooleanWrapper();
    private final List<Integer> areaOfInterest = new ArrayList<>();
    private final ObservableList<DstTile> dstTilesList = FXCollections.observableList(new ArrayList<>());
    private final ObjectProperty<Path> srcImagePath = new SimpleObjectProperty<>();
    private final IntegerProperty tilesPerRow = new SimpleIntegerProperty();
    private final IntegerProperty tilesPerImage = new SimpleIntegerProperty();
    private final ObjectProperty<Path> tilesPath = new SimpleObjectProperty<>();
    private final ObjectProperty<Path> cachePath = new SimpleObjectProperty<>();
    private final IntegerProperty tileSize = new SimpleIntegerProperty();
    private final IntegerProperty opacity = new SimpleIntegerProperty();
    private final IntegerProperty preColorAlignment = new SimpleIntegerProperty();
    private final IntegerProperty postColorAlignment = new SimpleIntegerProperty();
    private final ObjectProperty<Mode> mode = new SimpleObjectProperty<>(Mode.LINEAR_NEW);
    private final IntegerProperty reuseDistance = new SimpleIntegerProperty();
    private final IntegerProperty maxReuses = new SimpleIntegerProperty();
    private final IntegerProperty compareSize = new SimpleIntegerProperty();
    private final BooleanProperty scanSubFolder = new SimpleBooleanProperty();
    private final BooleanProperty drawDebugInfo = new SimpleBooleanProperty();
    private final BooleanProperty isTilesPerImage = new SimpleBooleanProperty();

    private final MosaikImage image = new MosaikImage();
    Task<Void> delayedTask = null;
    private Controller controller;
    private List<List<Integer>> scoredDstTileLists;
    private ObservableList<Integer> destinationTileIndexes;
    private DstTilesLoaderTask task;

    Thread compareThread;
    CompareTask compareTask;

    public MosaicImageModelImpl() {
        LOGGER.info("MosaicImageModelImpl");
        initChangeListener();
    }

    public void setController(Controller controller) {
        this.controller = controller;
    }

    private void initChangeListener() {
        LOGGER.info("initChangeListener");

        dstTilesLoadProgressProperty().addListener((observable, oldValue, newValue) -> Platform.runLater(() -> usedCount.set(0)));

        imageCalculatedProperty().addListener((observable, oldValue, newValue) -> Platform.runLater(() -> tilesMinNeeded.set(tilesPerRowProperty().get() * tilesPerColumnProperty().get() / getDivident())));

        dstTilesList.addListener((ListChangeListener<DstTile>) change -> Platform.runLater(() -> dstTilesCount.set(change.getList().size())));

        tilesPathProperty().addListener((observable, oldValue, newValue) -> loadDstTiles(newValue, cachePath.get()));
        cachePathProperty().addListener((observable, oldValue, newValue) -> loadDstTiles(tilesPath.get(), newValue));
        scanSubFolderProperty().addListener(((observable, oldValue, newValue) -> loadDstTiles(tilesPath.get(), cachePath.get())));

        srcImagePathProperty().addListener((observable, oldValue, newValue) -> loadImage(newValue, isTilesPerImage.get()));

        modeProperty().addListener((observable, oldValue, newValue) -> generateMosaicImage());
        reuseDistanceProperty().addListener((observable, oldValue, newValue) -> executeDelayed(this::generateMosaicImage));
        maxReusesProperty().addListener((observable, oldValue, newValue) -> executeDelayed(this::generateMosaicImage));

        opacityProperty().addListener((observable, oldValue, newValue) -> executeDelayed(newValue, this::setOpacityInTiles));
        preColorAlignmentProperty().addListener((observable, oldValue, newValue) -> executeDelayed(this::generateMosaicImage));

        postColorAlignmentProperty().addListener((observable, oldValue, newValue) -> executeDelayed(newValue, this::setPostColorAlignmentInTiles));

        tilesPerRowProperty().addListener((observable, oldValue, newValue) -> {
            if(!isTilesPerImage.get()) executeDelayed(() -> loadImage(srcImagePath.get(), isTilesPerImage.get()));
        });
        tilesPerImageProperty().addListener((observable, oldValue, newValue) -> executeDelayed(() -> {
            if(isTilesPerImage.get()) loadImage(srcImagePath.get(), isTilesPerImage.get());
        }));
        isTilesPerImageProperty().addListener((observable, oldValue, newValue) -> executeDelayed(() -> loadImage(srcImagePath.get(), isTilesPerImage.get())));
    }

    private int getDivident() {
        double maxReuses = maxReusesProperty().get() + 1;
        double norm2 = maxReuses / tilesPerRowProperty().get();
        double factor = 1.0 / ((1.0 - norm2) + reuseDistance.get() * norm2);
        return Math.max((int) (maxReuses * factor), 1);
    }

    private void executeDelayed(Number value, Consumer<Integer> numberConsumer) {
        if (delayedTask != null) delayedTask.cancel();
        delayedTask = getDelayedTask();
        delayedTask.setOnSucceeded(event -> numberConsumer.accept(value.intValue()));
        executeTask(delayedTask);
    }

    private void executeDelayed(Function func) {
        if (delayedTask != null) delayedTask.cancel();
        delayedTask = getDelayedTask();
        delayedTask.setOnSucceeded(event -> func.apply());
        executeTask(delayedTask);
    }

    private void executeTask(Task<Void> task) {
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private Task<Void> getDelayedTask() {
        return new Task<>() {
            @Override
            protected Void call() throws Exception {
                Thread.sleep(1000);
                return null;
            }
        };
    }

    private void setPostColorAlignmentInTiles(int postColorAlignment) {
        LOGGER.info("setPostColorAlignmentInTiles {}%", postColorAlignment);
        imageCalculated.set(false);
        image.setPostColorAlignment(postColorAlignment);
        imageCalculated.set(true);
    }

    private void setOpacityInTiles(int opacity) {
        LOGGER.info("setOpacityInTiles {}%", opacity);
        imageCalculated.set(false);
        image.setOpacity(opacity);
        imageCalculated.set(true);
    }

    private void loadDstTiles(Path tilesPath, Path cachePath) {
        if (tilesPath == null || cachePath == null) return;

        int tileSize = this.tileSize.get();
        int compareSize = this.compareSize.get();

        if (tileSize <= 0 || compareSize <= 0) return;
        LOGGER.info("loadDstTiles {}", tilesPath);

        if (task != null) task.cancel(true);
        boolean scanSubFolder = this.scanSubFolder.get();

        task = new DstTilesLoaderTask(tilesPath, cachePath, scanSubFolder, tileSize, compareSize);
        task.progressProperty().addListener((observable, oldValue, newValue) -> dstTilesLoadProgress.set((int) (newValue.doubleValue() * 100)));
        task.setOnSucceeded(event -> {
            LOGGER.info("{} images loaded", task.getValue().size());
            dstTilesList.clear();
            dstTilesList.addAll(task.getValue());
            generateMosaicImage();
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private void loadImage(Path path, boolean isTilesPerImage) {
        if (tileSize.get() <= 0 || (!isTilesPerImage && tilesPerRow.get() <= 0) || (isTilesPerImage && tilesPerImage.get() <= 0) || path == null) return;
        LOGGER.info("loadImage {}", path);

        Thread thread = new Thread(() -> {
            controller.setStatus("Lade Bild");

            try {
                BufferedImage bufferedImage = ImageIO.read(path.toFile());

                if(isTilesPerImage)
                {
                    int width = bufferedImage.getWidth();
                    int height = bufferedImage.getHeight();

                    int tilesPerRow = 1, tilesPerColumn;
                    do {
                        tilesPerColumn = tilesPerRow * height / width;
                        tilesPerRow++;
                    } while (tilesPerRow * tilesPerColumn < tilesPerImage.get());

                    controller.setTilesPerRow(tilesPerRow);

                    this.tilesPerColumn.set(Math.max(1, tilesPerColumn));
                }
                else
                {
                    controller.setTilesPerImage(tilesPerRow.get() * tilesPerColumn.get());
                    this.tilesPerColumn.set(Math.max(1, tilesPerRow.get() * bufferedImage.getHeight() / bufferedImage.getWidth()));
                }

                BufferedImage image = ImageTools.calculateScaledImage(bufferedImage,
                        tilesPerRow.get() * tileSize.get(),
                        this.tilesPerColumn.get() * tileSize.get(),
                        true);

                calculateTiles(image);
                generateMosaicImage();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            controller.setStatus("Bild geladen");
        });

        // don't let thread prevent JVM shutdown
        thread.setDaemon(true);
        thread.start();
    }

    private void calculateTiles(BufferedImage image) {
        LOGGER.info("calculateTiles");
        ImageTiler imageTiler = new ImageTiler(image, tileSize.get(), tilesPerRow.get(), tilesPerColumn.get());
        List<BufferedImage> tiles = imageTiler.getTiles();
        this.image.setTiles(tiles.stream().map(tileImage -> new OriginalTile(tileImage, compareSize.get())).toArray(OriginalTile[]::new));

        scoredDstTileLists = new ArrayList<>();
        for (int x = 0; x < tiles.size(); x++) {
            scoredDstTileLists.add(new ArrayList<>());
        }
    }

    protected List<Integer> getIndexSortedByScore(Map<Integer, Integer> scores) {
        List<Map.Entry<Integer, Integer>> list = new ArrayList<>(scores.entrySet());
        list.sort(Map.Entry.comparingByValue());
        return list.stream().mapToInt(Map.Entry::getKey).boxed().collect(Collectors.toList());
    }

    @Override
    public void addAreaOfIntrest(int x, int y) {
        int mosaikImageIndex = getIndex(x, y);
        if (!areaOfInterest.contains(mosaikImageIndex)) {
            areaOfInterest.add(mosaikImageIndex);
        }
        LOGGER.info("AreaOfInterest size = " + areaOfInterest.size());
    }

    @Override
    public void removeAreaOfIntrest(int x, int y) {
        areaOfInterest.remove((Object) getIndex(x, y)); // geht das besser?
        LOGGER.info("AreaOfInterest size = " + areaOfInterest.size());
    }

    @Override
    public void resetAreaOfIntrest() {
        areaOfInterest.clear();
    }

    @Override
    public BufferedImage getTile(int x, int y, boolean originalImage) {
        LOGGER.debug("getTile " + x + ", " + y);
        OriginalTile originalTile = image.getTile(getIndex(x, y));
        boolean noDestinationTile = dstTilesList.isEmpty() || destinationTileIndexes.get(getIndex(x, y)) == -1;
        return printDebugInformations((noDestinationTile || originalImage) ? originalTile.getSrcImage() : originalTile.getComposedImage(), x, y);
    }

    private BufferedImage printDebugInformations(BufferedImage srcImage, int x, int y) {
        DebugInformation debugInformation = getDebugInformation(x, y);
        LOGGER.debug("printDebugInformations " + drawDebugInfo.get());

        if (!drawDebugInfo.get()) return srcImage;
        BufferedImage bufferedImage = new BufferedImage(srcImage.getWidth(), srcImage.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = (Graphics2D) bufferedImage.getGraphics();
        g2d.drawImage(srcImage, 0, 0, null);

        g2d.setColor(Color.WHITE);
        g2d.fillRect(10, 0, 100, 11);
        g2d.fillRect(10, 15, 100, 11);
        g2d.fillRect(10, 30, 100, 11);
        g2d.fillRect(10, 45, 100, 11);
        g2d.fillRect(10, 60, 100, 11);

        g2d.setColor(Color.BLACK);
        g2d.drawString("x:" + debugInformation.x() + " y:" + debugInformation.y() + " i:" + debugInformation.mosaikArrayIndex(), 10, 10);
        g2d.drawString("aoi:" + debugInformation.areaOfIntrest(), 10, 25);
        g2d.drawString("index:" + debugInformation.destinationTileIndex(), 10, 40);
        g2d.drawString("r:" + debugInformation.reuses() + " pos:" + debugInformation.reusePositionsAsString(), 10, 55);
        g2d.drawString("stli:" + debugInformation.scoredTileListIndex(), 10, 70);

        return bufferedImage;
    }

    private DebugInformation getDebugInformation(int x, int y) {
        int index = getIndex(x, y);
        int destinationTileIndex = destinationTileIndexes.get(index);
        int[] positions = IntStream.range(0, destinationTileIndexes.size()).filter(i -> Objects.equals(destinationTileIndexes.get(i), destinationTileIndex) && i != index).toArray();
        return new DebugInformation(x, y, index, areaOfInterest.contains(index), destinationTileIndex, positions.length, positions, scoredDstTileLists.get(index).indexOf(destinationTileIndex));
    }

    private int getIndex(int x, int y) {
        LOGGER.trace("mosaikImageIndex from {},{}", x, y);
        return new Converter(tilesPerRow.get()).getIndex(new Position(x, y));
    }

    @Override
    public void generateMosaicImage() {
        LOGGER.info("generateMosaicImage");
        if (image.getLength() == 0) {
            LOGGER.info("MosaikImage noch nicht geladen");
            return;
        }

        if (dstTilesList.isEmpty()) {
            LOGGER.info("Destination Tile Liste noch nicht geladen");
            return;
        }

        if(compareThread != null) {
            compareThread.interrupt();
            if(compareTask != null) compareTask.cancel();
        }

        compareTask = getCompareTask();

        compareThread = new Thread(compareTask);
        compareThread.setDaemon(true);
        compareThread.start();
    }

    private CompareTask getCompareTask() {
        CompareTask compareTask = new CompareTask(
                image,
                dstTilesList,
                preColorAlignment.get()
        );

        compareTask.setOnScheduled(event -> controller.randomImageButton.setDisable(true));
        compareTask.setOnRunning(event -> controller.setStatus("Erzeuge Mosaikbild"));
        compareTask.progressProperty().addListener((observable, oldValue, newValue) -> dstTilesLoadProgress.set((int) (newValue.doubleValue() * 100)));
        compareTask.setOnSucceeded(event -> {
            controller.randomImageButton.setDisable(false);
            controller.setStatus("Mosaik erzeugt");
            scoredDstTileLists = compareTask.getValue();
            MosaikImageGenerateTask mosaikImageGenerateTask = new MosaikImageGenerateTask(
                    this,
                    scoredDstTileLists,
                    areaOfInterest
            );

            mosaikImageGenerateTask.setOnSucceeded(event2 -> {
                destinationTileIndexes = FXCollections.observableList(new ArrayList<>());
                destinationTileIndexes.addListener((ListChangeListener<Integer>) c -> Platform.runLater(() -> usedCount.set(destinationTileIndexes != null ? (int) destinationTileIndexes.stream().distinct().count() : 0)));
                destinationTileIndexes.addAll(mosaikImageGenerateTask.getValue());

                Platform.runLater(() -> {
                    imageCalculated.set(false);
                    controller.setStatus("Berechne Tiles");
                    image.unsetDstImages();
                    image.setOpacity(opacity.get());
                    image.setPostColorAlignment(postColorAlignment.get());
                    IntStream.range(0, destinationTileIndexes.size()).parallel().forEach(index -> {
                        int dstTileID = destinationTileIndexes.get(index);
                        if (dstTileID >= 0) {
                            image.getTile(index).setDstImage(dstTilesList.get(dstTileID).getImage());
                        }
                    });
                    imageCalculated.set(true);
                    controller.setStatus("Mosaikbild erstellt");
                });
            });

            Thread thread = new Thread(mosaikImageGenerateTask);
            thread.setDaemon(true);
            thread.start();
        });
        return compareTask;
    }

    @Override
    public String getDstTileInformation(int x, int y) {
        LOGGER.debug("getDstTileInformation {},{}", x, y);
        int index = getIndex(x, y);
        if (!dstTilesList.isEmpty() && destinationTileIndexes != null && index < destinationTileIndexes.size() && destinationTileIndexes.get(index) >= 0) {
            return dstTilesList.get(destinationTileIndexes.get(getIndex(x, y))).getFilename();
        }
        return "";
    }

    @Override
    public void saveMosaicImage(Path path) {
        LOGGER.info("saveMosaicImage {}", path);
        Thread thread = new Thread(new ImageSaver(path, image.getTiles(), tilesPerRow.get(), getTilesPerColumn(), tileSize.get(), controller));
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public ReadOnlyIntegerProperty dstTilesLoadProgressProperty() {
        return dstTilesLoadProgress.getReadOnlyProperty();
    }

    @Override
    public ObjectProperty<Path> tilesPathProperty() {
        return tilesPath;
    }

    @Override
    public ObjectProperty<Path> cachePathProperty() {
        return cachePath;
    }

    @Override
    public ObjectProperty<Path> srcImagePathProperty() {
        return srcImagePath;
    }

    @Override
    public IntegerProperty tileSizeProperty() {
        return tileSize;
    }

    @Override
    public IntegerProperty opacityProperty() {
        return opacity;
    }

    @Override
    public IntegerProperty preColorAlignmentProperty() {
        return preColorAlignment;
    }

    @Override
    public IntegerProperty postColorAlignmentProperty() {
        return postColorAlignment;
    }

    @Override
    public ObjectProperty<Mode> modeProperty() {
        return mode;
    }

    @Override
    public IntegerProperty reuseDistanceProperty() {
        return reuseDistance;
    }

    @Override
    public IntegerProperty maxReusesProperty() {
        return maxReuses;
    }

    @Override
    public IntegerProperty compareSizeProperty() {
        return compareSize;
    }

    @Override
    public BooleanProperty scanSubFolderProperty() {
        return scanSubFolder;
    }

    @Override
    public BooleanProperty drawDebugInfoProperty() {
        return drawDebugInfo;
    }

    @Override
    public IntegerProperty tilesPerRowProperty() {
        return tilesPerRow;
    }

    @Override
    public IntegerProperty tilesPerImageProperty() {
        return tilesPerImage;
    }

    @Override
    public BooleanProperty isTilesPerImageProperty() {
        return isTilesPerImage;
    }

    @Override
    public void replaceTile(int x, int y) {
        LOGGER.info("replaceTile {},{}", x, y);
        if (!dstTilesList.isEmpty() && destinationTileIndexes != null && destinationTileIndexes.get(getIndex(x, y)) >= 0) {
            int actualDestinationTileIndex = destinationTileIndexes.get(getIndex(x, y));
            List<Integer> scoredDstTileList = scoredDstTileLists.get(getIndex(x, y));
            int actualScoredListIndex = scoredDstTileList.indexOf(actualDestinationTileIndex);
            int nextScoredListIndex = getNextScoredListIndex(scoredDstTileList, actualScoredListIndex, getIndex(x, y), maxReuses.get(), reuseDistance.get());

            if (nextScoredListIndex >= 0) {
                LOGGER.info("replace");
                destinationTileIndexes.set(getIndex(x, y), scoredDstTileList.get(nextScoredListIndex));
                image.getTile(getIndex(x, y)).setDstImage(dstTilesList.get(scoredDstTileList.get(nextScoredListIndex)).getImage());
            } else {
                LOGGER.info("unset");
                destinationTileIndexes.set(getIndex(x, y), -1);
                image.getTile(getIndex(x, y)).setDstImage(null);
            }
        }
    }

    private int getNextScoredListIndex(List<Integer> scoredList, int actualScoredListIndex, int positionOfTile, int maxReuses, int reuseDistance) {
        int newScoredListIndex = actualScoredListIndex;

        while (++newScoredListIndex < scoredList.size()) {
            int dstTileIndex = scoredList.get(newScoredListIndex);
            long reuses = destinationTileIndexes.stream().filter(value -> value == dstTileIndex).count();
            boolean isReuseable = reuses <= maxReuses;

            int[] sameTiles = IntStream.range(0, destinationTileIndexes.size())
                    .filter(i -> Objects.equals(destinationTileIndexes.get(i), dstTileIndex)).toArray();

            boolean hasTileInDistsance = Arrays.stream(sameTiles).anyMatch(position -> new StraightTileDistanceCalculator(tilesPerRow.get()).calculate(positionOfTile, position) < reuseDistance);

            LOGGER.info("test tile " + dstTileIndex);
            LOGGER.info("hasTileInDistsance: " + hasTileInDistsance + " index:" + positionOfTile + " sameTiles: " + Arrays.toString(sameTiles) + " reuseDistance: " + reuseDistance);
            LOGGER.info("isReusanle:" + isReuseable + " reuses:" + reuses + " maxReuses:" + maxReuses);

            if (isReuseable && !hasTileInDistsance) return newScoredListIndex;
        }

        return -1;
    }

    @Override
    public void ignoreTile(int x, int y) {
        LOGGER.info("ignoreTile {},{}", x, y);
        if (!dstTilesList.isEmpty() && destinationTileIndexes.get(getIndex(x, y)) >= 0) {
            int actualDestinationTileIndex = destinationTileIndexes.get(getIndex(x, y));
            LOGGER.info("replace image " + actualDestinationTileIndex);

            int count = 0;
            for (int imageX = 0; imageX < tilesPerRow.get(); imageX++) {
                for (int imageY = 0; imageY < tilesPerColumn.get(); imageY++) {
                    int index = getIndex(imageX, imageY);
                    if (destinationTileIndexes.get(index) == actualDestinationTileIndex) {
                        LOGGER.info("Bild {} an Position {} gefunden", actualDestinationTileIndex, index);
                        replaceTile(imageX, imageY);
                        count++;
                    }
                }
            }
            LOGGER.info("replaced " + count + " Tiles.");
            removeTileIndexFromScoredLists(actualDestinationTileIndex);
        }
    }

    private void removeTileIndexFromScoredLists(Integer actualDestinationTileIndex) {
        for (List<Integer> scoredDstTileList : scoredDstTileLists) {
            scoredDstTileList.remove(actualDestinationTileIndex);
        }
    }

    @Override
    public ReadOnlyBooleanProperty imageCalculatedProperty() {
        return imageCalculated.getReadOnlyProperty();
    }

    @Override
    public ReadOnlyIntegerProperty tilesPerColumnProperty() {
        return tilesPerColumn.getReadOnlyProperty();
    }

    @Override
    public int getTilesPerColumn() {
        return tilesPerColumn.get();
    }

    @Override
    public ReadOnlyIntegerProperty dstTilesCountProperty() {
        return dstTilesCount.getReadOnlyProperty();
    }

    @Override
    public ReadOnlyIntegerProperty usedCountProperty() {
        return usedCount.getReadOnlyProperty();
    }

    @Override
    public ReadOnlyIntegerProperty tilesMinNeededProperty() {
        return tilesMinNeeded.getReadOnlyProperty();
    }

    @FunctionalInterface
    interface Function {
        void apply();
    }
}
