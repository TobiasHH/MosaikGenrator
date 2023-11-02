package de.tobiashh.javafx.model;

import de.tobiashh.javafx.DstTilesLoaderTask;
import de.tobiashh.javafx.Mode;
import de.tobiashh.javafx.TilesStraightDistance;
import de.tobiashh.javafx.composer.*;
import de.tobiashh.javafx.save.ImageSaver;
import de.tobiashh.javafx.tiles.DstTile;
import de.tobiashh.javafx.tiles.OriginalTile;
import de.tobiashh.javafx.tools.ImageTools;
import de.tobiashh.javafx.tools.Index2D;
import de.tobiashh.javafx.tools.IndexConverter;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MosaicImageModelImpl implements MosaicImageModel {
    public static final String[] FILE_EXTENSION = {"png", "jpg", "jpeg"};
    private final static Logger LOGGER = LoggerFactory.getLogger(MosaicImageModelImpl.class.getName());
    private final ReadOnlyIntegerWrapper dstTilesCount = new ReadOnlyIntegerWrapper();
    private final ReadOnlyIntegerWrapper tilesPerColumn = new ReadOnlyIntegerWrapper();
    private final ReadOnlyIntegerWrapper dstTilesLoadProgress = new ReadOnlyIntegerWrapper();
    private final ReadOnlyStringWrapper status = new ReadOnlyStringWrapper();
    private final ReadOnlyBooleanWrapper imageCalculated = new ReadOnlyBooleanWrapper();
    private final List<Integer> areaOfInterest = new ArrayList<>();
    private final ObservableList<DstTile> dstTilesList = FXCollections.observableList(new ArrayList<>());
    private final ObjectProperty<Path> srcImagePath = new SimpleObjectProperty<>();
    private final IntegerProperty tilesPerRow = new SimpleIntegerProperty();
    private final ObjectProperty<Path> tilesPath = new SimpleObjectProperty<>();
    private final ObjectProperty<Path> cachePath = new SimpleObjectProperty<>();
    private final IntegerProperty tileSize = new SimpleIntegerProperty();
    private final IntegerProperty opacity = new SimpleIntegerProperty();
    private final IntegerProperty postColorAlignment = new SimpleIntegerProperty();
    private final ObjectProperty<Mode> mode = new SimpleObjectProperty<>(Mode.LINEAR_NEW);
    private final IntegerProperty reuseDistance = new SimpleIntegerProperty();
    private final IntegerProperty maxReuses = new SimpleIntegerProperty();
    private final IntegerProperty compareSize = new SimpleIntegerProperty();
    private final BooleanProperty scanSubFolder = new SimpleBooleanProperty();
    private final BooleanProperty drawDebugInfo = new SimpleBooleanProperty();

    private final MosaikImage image = new MosaikImage();
    private List<List<Integer>> scoredDstTileLists;
    private List<Integer> destinationTileIndexes;

    private DstTilesLoaderTask task;

    public MosaicImageModelImpl() {
        LOGGER.info("MosaicImageModelImpl");
        initChangeListener();
    }

    private void initChangeListener() {
        LOGGER.info("initChangeListener");

        dstTilesLoadProgressProperty().addListener((observable, oldValue, newValue) -> dstTilesCount.set(newValue.intValue()));
        dstTilesList.addListener((ListChangeListener<DstTile>) change -> dstTilesCount.set(change.getList().size()));

        tilesPathProperty().addListener((observable, oldValue, newValue) -> loadDstTiles(newValue, cachePath.get()));
        cachePathProperty().addListener((observable, oldValue, newValue) -> loadDstTiles(tilesPath.get(), newValue));
        scanSubFolderProperty().addListener(((observable, oldValue, newValue) -> loadDstTiles(tilesPath.get(), cachePath.get())));

        srcImagePathProperty().addListener((observable, oldValue, newValue) -> loadImage(newValue));

        modeProperty().addListener((observable, oldValue, newValue) -> generateMosaicImage());
        reuseDistanceProperty().addListener((observable, oldValue, newValue) -> generateMosaicImage());
        maxReusesProperty().addListener((observable, oldValue, newValue) -> generateMosaicImage());

        opacityProperty().addListener((observable, oldValue, newValue) -> setOpacityInTiles(newValue.intValue()));
        postColorAlignmentProperty().addListener((observable, oldValue, newValue) -> setPostColorAlignmentInTiles(newValue.intValue()));

        tilesPerRowProperty().addListener((observable, oldValue, newValue) -> loadImage(srcImagePath.get()));
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

    private void loadImage(Path path) {
        if (tileSize.get() <= 0 || tilesPerRow.get() <= 0 || path == null) return;
        LOGGER.info("loadImage {}", path);

        Thread thread = new Thread(() -> {
            Platform.runLater(() -> status.set("Lade Bild"));

            try {
                BufferedImage bufferedImage = ImageIO.read(path.toFile());

                int tilesPerColumn = Math.max(1, tilesPerRow.get() * bufferedImage.getHeight() / bufferedImage.getWidth());

                this.tilesPerColumn.set(tilesPerColumn);

                BufferedImage image = ImageTools.calculateScaledImage(bufferedImage,
                        tilesPerRow.get() * tileSize.get(),
                        tilesPerColumn * tileSize.get(),
                        true);

                calculateTiles(image);
                generateMosaicImage();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Platform.runLater(() -> status.set("Bild geladen"));
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

    private void compareTiles(OriginalTile[] mosaicImage, ObservableList<DstTile> dstTilesList) {
        LOGGER.info("compareTiles");

        IntStream.range(0, mosaicImage.length).parallel().forEach(mosaikImageIndex -> {
            Map<Integer, Integer> scores = new HashMap<>();

            for (int index = 0; index < dstTilesList.size(); index++) {
                scores.put(index, mosaicImage[mosaikImageIndex].compare(dstTilesList.get(index)));
            }

            scoredDstTileLists.get(mosaikImageIndex).clear();
            scoredDstTileLists.get(mosaikImageIndex).addAll(getIndexSortedByScore(scores));
        });
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
        return printDebugInformations((noDestinationTile || originalImage) ? originalTile.getSrcImage() : originalTile.getComposedImage() , x, y);
    }

    private BufferedImage printDebugInformations(BufferedImage srcImage, int x, int y) {
        LOGGER.debug("printDebugInformations " + drawDebugInfo.get());
        if (!drawDebugInfo.get()) return srcImage;
        BufferedImage bufferedImage = new BufferedImage(srcImage.getWidth(), srcImage.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = (Graphics2D) bufferedImage.getGraphics();
        g2d.drawImage(srcImage, 0, 0, null);
        int destinationTileIndex = destinationTileIndexes.get(getIndex(x, y));
        int[] positions = IntStream.range(0, destinationTileIndexes.size())
                .filter(i -> Objects.equals(destinationTileIndexes.get(i), destinationTileIndex) && i != getIndex(x, y)).toArray();
        int scoredTileListIndex = scoredDstTileLists.get(getIndex(x, y)).indexOf(destinationTileIndex);

        int reuses = positions.length;

        g2d.setColor(Color.WHITE);
        g2d.fillRect(10, 0, 100, 11);
        g2d.fillRect(10, 15, 100, 11);
        g2d.fillRect(10, 30, 100, 11);
        g2d.fillRect(10, 45, 100, 11);
        g2d.fillRect(10, 60, 100, 11);

        g2d.setColor(Color.BLACK);
        g2d.drawString("x:" + x + " y:" + y + " i:" + getIndex(x, y), 10, 10);
        g2d.drawString("aoi:" + areaOfInterest.contains(getIndex(x, y)), 10, 25);
        g2d.drawString("index:" + destinationTileIndex, 10, 40);
        g2d.drawString("r:" + reuses + " pos:" + Arrays.toString(positions), 10, 55);
        g2d.drawString("stli:" + scoredTileListIndex, 10, 70);

        return bufferedImage;
    }


    private int getIndex(int x, int y) {
        LOGGER.trace("mosaikImageIndex from {},{}", x, y);
        return new IndexConverter(tilesPerRow.get()).convert2DToLinear(new Index2D(x, y));
    }

    @Override
    public void generateMosaicImage() {

        if (image.getLength() == 0 || dstTilesList.size() == 0) return;
        LOGGER.info("generateMosaicImage");

        imageCalculated.set(false);
        // TODO muss das huer passieren auch wenn nur von linear zu random gewechselt wurde aber src und dswt gleich bleiben
        compareTiles(image.getTiles(), dstTilesList);

        image.unsetDstImages();

        checkIntegrity(dstTilesList, scoredDstTileLists);

        LOGGER.info("generate image start");
        destinationTileIndexes = ImageComposerFactory
                .getComposer(mode.get())
                .generate(tilesPerRow.get()
                        , tilesPerColumn.get()
                        , maxReuses.get()
                        , reuseDistance.get()
                        , areaOfInterest
                        , scoredDstTileLists);
        LOGGER.info("generate image finished");

        image.setOpacity(opacity.get());
        image.setPostColorAlignment(postColorAlignment.get());

        IntStream.range(0, destinationTileIndexes.size()).parallel().forEach(index -> {
            int dstTileID = destinationTileIndexes.get(index);
            if (dstTileID >= 0) {
                image.getTile(index).setDstImage(dstTilesList.get(dstTileID).getImage());
            }
        });

        imageCalculated.set(true);
    }

    private void checkIntegrity(ObservableList<DstTile> dstTilesList, List<List<Integer>> scoredDstTileLists) {
        for (List<Integer> scoredDstTileList : scoredDstTileLists) {
            if (dstTilesList.size() != scoredDstTileList.size()) {
                LOGGER.warn("scored list not correct size");
                System.exit(-1);
            }
        }
    }

    @Override
    public String getDstTileInformation(int x, int y) {
        LOGGER.debug("getDstTileInformation {},{}", x, y);
        if (!dstTilesList.isEmpty() && destinationTileIndexes.get(getIndex(x, y)) >= 0) {
            return dstTilesList.get(destinationTileIndexes.get(getIndex(x, y))).getFilename();
        }
        return "";
    }

    @Override
    public void saveMosaicImage(Path path) {
        LOGGER.info("saveMosaicImage {}", path);
        Thread thread = new Thread(new ImageSaver(path, image.getTiles(), tilesPerRow.get(), getTilesPerColumn(), tileSize.get()));
        thread.setDaemon(true);
        thread.start();
    }

    private ReadOnlyIntegerProperty dstTilesLoadProgressProperty() {
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
    public void replaceTile(int x, int y) {
        if (!dstTilesList.isEmpty() && destinationTileIndexes.get(getIndex(x, y)) >= 0) {
            int actualDestinationTileIndex = destinationTileIndexes.get(getIndex(x, y));
            List<Integer> scoredDstTileList = scoredDstTileLists.get(getIndex(x, y));
            int actualScoredListIndex = scoredDstTileList.indexOf(actualDestinationTileIndex);

            int nextScoredListIndex = getNextScoredListIndex(scoredDstTileList, actualScoredListIndex, getIndex(x, y), maxReuses.get(), reuseDistance.get());

            if (nextScoredListIndex >= 0) {
                destinationTileIndexes.set(getIndex(x, y), scoredDstTileList.get(nextScoredListIndex));
                image.getTile(getIndex(x, y)).setDstImage(dstTilesList.get(scoredDstTileList.get(nextScoredListIndex)).getImage());
            } else {
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

            boolean hasTileInDistsance = Arrays.stream(sameTiles).anyMatch(position -> new TilesStraightDistance(tilesPerRow.get()).calculate(positionOfTile, position) < reuseDistance);

            LOGGER.info("test tile " + dstTileIndex);
            LOGGER.info("hasTileInDistsance: " + hasTileInDistsance + " index:" + positionOfTile + " sameTiles: " + Arrays.toString(sameTiles) + " reuseDistance: " + reuseDistance);
            LOGGER.info("isReusanle:" + isReuseable + " reuses:" + reuses + " maxReuses:" + maxReuses);

            if (isReuseable && !hasTileInDistsance) return newScoredListIndex;
        }

        return -1;
    }

    @Override
    public void ignoreTile(int x, int y) {
        if (!dstTilesList.isEmpty() && destinationTileIndexes.get(getIndex(x, y)) >= 0) {
            int actualDestinationTileIndex = destinationTileIndexes.get(getIndex(x, y));
            LOGGER.info("replace image " + actualDestinationTileIndex);

            int count = 0;
            for (int imageX = 0; imageX < tilesPerRow.get(); imageX++) {
                for (int imageY = 0; imageY < tilesPerColumn.get(); imageY++) {
                    int index = getIndex(imageX, imageY);
                    if (destinationTileIndexes.get(index) == actualDestinationTileIndex) {
                        LOGGER.info("Bild " + actualDestinationTileIndex + " an Position " + index + " gefunden.");
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
    public ReadOnlyStringWrapper statusProperty() {
        return status;
    }

    @Override
    public ReadOnlyIntegerProperty dstTilesCountProperty() {
        return dstTilesCount.getReadOnlyProperty();
    }
}
