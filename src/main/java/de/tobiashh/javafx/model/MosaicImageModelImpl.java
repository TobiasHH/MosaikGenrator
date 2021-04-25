package de.tobiashh.javafx.model;

import de.tobiashh.javafx.DstTilesLoaderTask;
import de.tobiashh.javafx.TilesCircularDistance;
import de.tobiashh.javafx.TilesStraightDistance;
import de.tobiashh.javafx.save.ImageSaver;
import de.tobiashh.javafx.tiles.DstTile;
import de.tobiashh.javafx.tiles.OriginalTile;
import de.tobiashh.javafx.tools.ImageTools;
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

// TODO comparableImage aus tiles herauslösen
// TODO loadTiles in Controller bzw extra Klasse
// TODO Aufräumen und Methoden sortieren
// TODO ImagePool für DST Images
// TODO DST Images besitzen  liste von Indexes wo sie genutzt sind
// TODO ImageComparator erstellen
// TODO Pair Klasse von tile und comparable image
// TODO ImageComparator hat jeweils 1 SrcTilePair und List preAdjusted dstTilesPairs
// TODO Generator Klassen
// TODO abklären wie sich garbage collection beim aufräumen verhält
// TODO scrollpane / canvas nur ausschnitt berechnen (Zoom Problem)) durch canvas tiles mit z.b. 1000 x 1000 px
// TODO preColorAlignment implementieren
// TODO Bilder ausschließen generell
// TODO Bild ausschließen an einer Position
// TODO Gibt es bessere Vergleichsalgorhytmen? die z.b. stärker auf kontruren / Details achten? z.b. kantenerkennung und diese kanten mit einbeziehen
// TODO tests
// TODO tileWidth 512 führt zu HEAP da zu viele/Große Bilder in Tile Klassen gehalten werden
// TODO schnelles ändern der tilesPerRow provoziert exception zb. svcgnell 5 0 engeben

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
    private final IntegerProperty tileSize = new SimpleIntegerProperty();
    private final IntegerProperty opacity = new SimpleIntegerProperty();
    private final IntegerProperty postColorAlignment = new SimpleIntegerProperty();
    private final ObjectProperty<Mode> mode = new SimpleObjectProperty<>(Mode.LINEAR);
    private final IntegerProperty reuseDistance = new SimpleIntegerProperty();
    private final IntegerProperty maxReuses = new SimpleIntegerProperty();
    private final IntegerProperty compareSize = new SimpleIntegerProperty();
    private final BooleanProperty scanSubFolder = new SimpleBooleanProperty();

    private OriginalTile[] mosaicImage;
    private DstTilesLoaderTask task;

    private TilesStraightDistance tilesStraightDistance;
    private TilesCircularDistance tilesCircularDistance;

    public MosaicImageModelImpl() {
        LOGGER.info("MosaicImageModelImpl");
        tilesStraightDistance = new TilesStraightDistance(tilesPerRow.get());
        tilesCircularDistance = new TilesCircularDistance(tilesPerRow.get());
        tilesPerRow.addListener((observable, oldValue, newValue) -> {
            tilesStraightDistance = new TilesStraightDistance(newValue.intValue());
            tilesCircularDistance = new TilesCircularDistance(newValue.intValue());
        });
        initChangeListener();

    }

    private void initChangeListener() {
        LOGGER.info("initChangeListener");

        dstTilesLoadProgressProperty().addListener((observable, oldValue, newValue) -> dstTilesCount.set(newValue.intValue()));
        dstTilesList.addListener((ListChangeListener<DstTile>) change -> dstTilesCount.set(change.getList().size()));

        tilesPathProperty().addListener((observable, oldValue, newValue) -> loadDstTiles(newValue));
        scanSubFolderProperty().addListener(((observable, oldValue, newValue) -> loadDstTiles(tilesPath.get())));

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
        for (OriginalTile originalTile : mosaicImage) {
            originalTile.setPostColorAlignment(postColorAlignment);
        }
        imageCalculated.set(true);
    }

    private void setOpacityInTiles(int opacity) {
        LOGGER.info("setOpacityInTiles {}%", opacity);
        imageCalculated.set(false);
        for (OriginalTile originalTile : mosaicImage) {
            originalTile.setOpacity(opacity);
        }
        imageCalculated.set(true);
    }

    private void loadDstTiles(Path path) {
        int tileSize = this.tileSize.get();
        int compareSize = this.compareSize.get();

        if (tileSize <= 0 || compareSize <= 0) return;
        LOGGER.info("loadDstTiles {}", path);

        if (task != null) task.cancel(true);
        boolean scanSubFolder = this.scanSubFolder.get();

        task = new DstTilesLoaderTask(path, scanSubFolder, tileSize, compareSize);
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
        mosaicImage = new OriginalTile[getTilesPerColumn() * tilesPerRow.get()];
        for (int y = 0; y < getTilesPerColumn(); y++) {
            for (int x = 0; x < tilesPerRow.get(); x++) {
                BufferedImage subImage = image.getSubimage(x * tileSize.get(), y * tileSize.get(), tileSize.get(), tileSize.get());
                mosaicImage[index(x, y)] = new OriginalTile(subImage, compareSize.get());
            }
        }
    }

    private void compareTiles(OriginalTile[] mosaicImage, ObservableList<DstTile> dstTilesList) {
        LOGGER.info("compareTiles");

        for (OriginalTile originalTile : mosaicImage) {
            Map<Integer, Integer> scores = new HashMap<>();

            for (int i = 0; i < dstTilesList.size(); i++) {
                scores.put(i, originalTile.compare(dstTilesList.get(i)));
            }

            List<Map.Entry<Integer, Integer>> list = new ArrayList<>(scores.entrySet());
            list.sort(Map.Entry.comparingByValue());
            int[] keys = list.stream().mapToInt(Map.Entry::getKey).toArray();
            originalTile.setDstTileIDs(keys);
        }
    }

    @Override
    public void addAreaOfIntrest(int x, int y) {
        System.out.println("MosaicImageModelImpl.addAreaOfIntrest");
        int index = index(x, y);
        if (!areaOfInterest.contains(index)) {
            areaOfInterest.add(index);
        }
        System.out.println(areaOfInterest.size());
    }

    @Override
    public void removeAreaOfIntrest(int x, int y) {
        System.out.println("MosaicImageModelImpl.removeAreaOfIntrest");
        areaOfInterest.remove((Object) index(x, y)); // geht das besser?
        System.out.println(areaOfInterest.size());
    }

    @Override
    public BufferedImage getTile(int x, int y) {
        LOGGER.debug("getTile " + x + ", " + y);
        OriginalTile originalTile = mosaicImage[index(x, y)];
        boolean noDestinationTile = dstTilesList.isEmpty() || originalTile.getDstTileID() == -1;
        return printDebugInformations((noDestinationTile) ? originalTile.getSrcImage() : originalTile.getComposedImage(), x, y);
    }

    private BufferedImage printDebugInformations(BufferedImage srcImage, int x, int y) {
        BufferedImage bufferedImage = new BufferedImage(srcImage.getWidth(), srcImage.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = (Graphics2D) bufferedImage.getGraphics();
        g2d.drawImage(srcImage, 0, 0, null);
        g2d.setColor(Color.red);
        g2d.drawString(x + ", " + y, 10, 10);
        g2d.drawString("" + areaOfInterest.contains(index(x, y)), 10, 25);
        return bufferedImage;
    }


    private int index(int x, int y) {
        LOGGER.trace("index from {},{}", x, y);
        return y * tilesPerRow.get() + x;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean setDstTileIndex(OriginalTile actualTile, OriginalTile[] tiles) {
        LOGGER.debug("setDstTileIndex");
        if (actualTile.getDstTileIndex() > -1) return false;

        actualTile.incrementDstTileIndex();

        if (actualTile.getDstTileIndex() == -1) return false;

        int tileID = actualTile.getDstTileID();

        if (Arrays.stream(mosaicImage).filter(t -> t.getDstTileID() == tileID).count() > maxReuses.get()) {
            blockID(tileID, actualTile, tiles);
        }

        int actualIndex = IntStream.range(0, tiles.length).filter(i -> tiles[i] == actualTile).findFirst().orElse(-1);
        IntStream.range(0, tiles.length).forEach(i -> {
            if (tilesStraightDistance.calculate(actualIndex, i) < reuseDistance.get()) {
                tiles[i].addBlockedIds(tileID);
            }
        });

        return true;
    }

    @Override
    public void generateMosaicImage() {

        if (mosaicImage == null || mosaicImage.length == 0 || dstTilesList.size() == 0) return;
        LOGGER.info("generateMosaicImage");

        imageCalculated.set(false);
        // TODO muss das huer passieren auch wenn nur von linear zu random gewechselt wurde aber src und dswt gleich bleiben
        compareTiles(mosaicImage, dstTilesList);

        for (OriginalTile originalTile : mosaicImage) {
            originalTile.setDstImage(null);
        }


        Arrays.stream(mosaicImage).forEach(OriginalTile::resetIndex);

        if (mode.get() == Mode.TILE_DISTANCE) {
            generateCenterDistanceImage();
        } else if (mode.get() == Mode.CIRCULAR) {
            generateCircularImage();
        } else if (mode.get() == Mode.RANDOM) {
            generateRandomImage();
        } else {
            generateLinearImage();
        }

        for (OriginalTile originalTile : mosaicImage) {
            int dstTileID = originalTile.getDstTileID();
            if (dstTileID >= 0) {
                originalTile.setOpacity(opacity.get());
                originalTile.setPostColorAlignment(postColorAlignment.get());
                originalTile.setDstImage(dstTilesList.get(dstTileID).getImage());
            }
        }

        imageCalculated.set(true);
    }

    private void generateCircularImage() {
        LOGGER.info("generateCircularImage");
        OriginalTile[] mosaicImage = this.mosaicImage;
        ObservableList<DstTile> dstTilesList = this.dstTilesList;
        int tilesPerRow = this.tilesPerRow.get();
        int tilesPerColumn = getTilesPerColumn();
        List<Integer> areaOfInterest = this.areaOfInterest;

        List<Integer> tileIndices = IntStream.range(0, tilesPerColumn * tilesPerRow).boxed().collect(Collectors.toList());
        List<Integer> areaOfIntrestIndices = new ArrayList<>(areaOfInterest);
        tileIndices.removeAll(areaOfIntrestIndices);
        int startIndex = index(tilesPerRow / 2, tilesPerColumn / 2);

        while (areaOfIntrestIndices.size() > 0) {
            Integer index = areaOfIntrestIndices
                    .stream()
                    .min(Comparator.comparingInt(value -> tilesCircularDistance.calculate(value, startIndex))).get();
            if (!setDstTileIndex(mosaicImage[index], mosaicImage)) return;
            areaOfIntrestIndices.remove(index);
        }

        while (tileIndices.size() > 0) {
            Integer index = tileIndices.stream()
                    .min(Comparator.comparingInt(value -> tilesCircularDistance.calculate(value, startIndex))).get();
            if (!setDstTileIndex(mosaicImage[index], mosaicImage)) return;
            tileIndices.remove(index);
        }
    }

    private void generateCenterDistanceImage() {
        LOGGER.info("generateCenterDistanceImage");
        OriginalTile[] mosaicImage = this.mosaicImage;
        int tilesPerRow = this.tilesPerRow.get();
        int tilesPerColumn = getTilesPerColumn();
        List<Integer> areaOfInterest = this.areaOfInterest;

        List<Integer> tileIndices = IntStream.range(0, tilesPerColumn * tilesPerRow).boxed().collect(Collectors.toList());
        List<Integer> areaOfIntrestIndices = new ArrayList<>(areaOfInterest);
        tileIndices.removeAll(areaOfIntrestIndices);
        int startIndex = index(tilesPerRow / 2, tilesPerColumn / 2);

        while (areaOfIntrestIndices.size() > 0) {
            Integer index = areaOfIntrestIndices
                    .stream()
                    .min(Comparator.comparingInt(value -> tilesStraightDistance.calculate(value, startIndex))).get();
            if (!setDstTileIndex(mosaicImage[index], mosaicImage)) return;
            areaOfIntrestIndices.remove(index);
        }

        while (tileIndices.size() > 0) {
            Integer index = tileIndices.stream()
                    .min(Comparator.comparingInt(value -> tilesStraightDistance.calculate(value, startIndex))).get();
            if (!setDstTileIndex(mosaicImage[index], mosaicImage)) return;
            tileIndices.remove(index);
        }

    }

    @Override
    public String getDstTileInformation(int x, int y) {
        LOGGER.debug("getDstTileInformation {},{}", x, y);
        if (!dstTilesList.isEmpty() && mosaicImage[index(x, y)].getDstTileID() >= 0) {
            return dstTilesList.get(mosaicImage[index(x, y)].getDstTileID()).getFilename();
        }
        return "";
    }

    @Override
    public void saveMosaicImage(Path path) {
        LOGGER.info("saveMosaicImage {}", path);
        Thread thread = new Thread(new ImageSaver(path, mosaicImage, tilesPerRow.get(), getTilesPerColumn(), tileSize.get()));
        thread.setDaemon(true);
        thread.start();
    }

    private void generateRandomImage() {
        LOGGER.info("generateRandomImage");
        OriginalTile[] mosaicImage = this.mosaicImage;
        int tilesPerRow = this.tilesPerRow.get();
        int tilesPerColumn = getTilesPerColumn();
        List<Integer> areaOfInterest = this.areaOfInterest;

        Random rand = new Random();

        List<Integer> tileIndices = IntStream.range(0, tilesPerColumn * tilesPerRow).boxed().collect(Collectors.toList());
        List<Integer> areaOfInterestIndices = new ArrayList<>(areaOfInterest);
        tileIndices.removeAll(areaOfInterestIndices);

        while (areaOfInterestIndices.size() > 0) {
            if (!setDstTileIndex(mosaicImage[areaOfInterestIndices.remove(rand.nextInt(areaOfInterestIndices.size()))], mosaicImage))
                return;
        }

        while (tileIndices.size() > 0) {
            if (!setDstTileIndex(mosaicImage[tileIndices.remove(rand.nextInt(tileIndices.size()))], mosaicImage))
                return;
        }
    }

    private void generateLinearImage() {
        LOGGER.info("generateLinearImage");
        OriginalTile[] mosaicImage = this.mosaicImage;
        int tilesPerRow = this.tilesPerRow.get();
        int tilesPerColumn = getTilesPerColumn();
        List<Integer> areaOfInterest = this.areaOfInterest;


        for (int y = 0; y < tilesPerColumn; y++) {
            for (int x = 0; x < tilesPerRow; x++) {
                if (areaOfInterest.contains(index(x, y))) {
                    if (!setDstTileIndex(mosaicImage[index(x, y)], mosaicImage)) return;
                }
            }
        }

        for (int y = 0; y < tilesPerColumn; y++) {
            for (int x = 0; x < tilesPerRow; x++) {
                if (!areaOfInterest.contains(index(x, y))) {
                    if (!setDstTileIndex(mosaicImage[index(x, y)], mosaicImage)) return;
                }
            }
        }
    }

    private void blockID(int id, OriginalTile tile, OriginalTile[] mosaicImage) {
        LOGGER.debug("blockID");
        for (OriginalTile originalTile : mosaicImage) {
            if (originalTile != tile) {
                originalTile.addBlockedIds(id);
            }
        }
    }

    private ReadOnlyIntegerProperty dstTilesLoadProgressProperty() {
        return dstTilesLoadProgress.getReadOnlyProperty();
    }

    @Override
    public BufferedImage getOriginalTile(int x, int y) {
        LOGGER.debug("getOriginalTile " + x + ", " + y);
        return printDebugInformations(mosaicImage[index(x, y)].getSrcImage(), x, y);
    }

    @Override
    public ObjectProperty<Path> tilesPathProperty() {
        return tilesPath;
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
    public IntegerProperty tilesPerRowProperty() {
        return tilesPerRow;
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
