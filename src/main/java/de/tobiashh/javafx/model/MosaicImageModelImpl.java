package de.tobiashh.javafx.model;

import de.tobiashh.javafx.DstTilesLoaderTask;
import de.tobiashh.javafx.Mode;
import de.tobiashh.javafx.composer.*;
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
    private final ObjectProperty<Mode> mode = new SimpleObjectProperty<>(Mode.LINEAR_NEW);
    private final IntegerProperty reuseDistance = new SimpleIntegerProperty();
    private final IntegerProperty maxReuses = new SimpleIntegerProperty();
    private final IntegerProperty compareSize = new SimpleIntegerProperty();
    private final BooleanProperty scanSubFolder = new SimpleBooleanProperty();

    private final MosaikImage image = new MosaikImage();
    private List<List<Integer>> destinationTilesIDs;
    private List<Integer> destinationImageIndexes;

    private DstTilesLoaderTask task;

    public MosaicImageModelImpl() {
        LOGGER.info("MosaicImageModelImpl");
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
        image.setPostColorAlignment(postColorAlignment);
        imageCalculated.set(true);
    }

    private void setOpacityInTiles(int opacity) {
        LOGGER.info("setOpacityInTiles {}%", opacity);
        imageCalculated.set(false);
        image.setOpacity(opacity);
        imageCalculated.set(true);
    }

    private void loadDstTiles(Path path) {
        if (path == null) return;

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
        ImageTiler imageTiler = new ImageTiler(image, tileSize.get(), tilesPerRow.get(), tilesPerColumn.get());
        List<BufferedImage> tiles = imageTiler.getTiles();
        this.image.setTiles( tiles.stream().map(tileImage -> new OriginalTile(tileImage, compareSize.get())).toArray(OriginalTile[]::new));

        destinationTilesIDs = new ArrayList<>();
        for (int x = 0; x < tiles.size(); x++) {
            destinationTilesIDs.add(new ArrayList<>());
        }
    }

    private void compareTiles(OriginalTile[] mosaicImage, ObservableList<DstTile> dstTilesList) {
        LOGGER.info("compareTiles");

        IntStream.range(0, mosaicImage.length).forEach(index -> {
            Map<Integer, Integer> scores = new HashMap<>();

            for (int dstTilesListIndex = 0; dstTilesListIndex < dstTilesList.size(); dstTilesListIndex++) {
                scores.put(dstTilesListIndex, mosaicImage[index].compare(dstTilesList.get(dstTilesListIndex)));
            }

            List<Map.Entry<Integer, Integer>> list = new ArrayList<>(scores.entrySet());
            list.sort(Map.Entry.comparingByValue());
            List<Integer> keys = list.stream().mapToInt(Map.Entry::getKey).boxed().collect(Collectors.toList());
            destinationTilesIDs.get(index).clear();
            destinationTilesIDs.get(index).addAll(keys);
        });
    }

    @Override
    public void addAreaOfIntrest(int x, int y) {
        int mosaikImageIndex = mosaikImageIndex(x, y);
        if (!areaOfInterest.contains(mosaikImageIndex)) {
            areaOfInterest.add(mosaikImageIndex);
        }
        LOGGER.info("AreaOfInterest size = " + areaOfInterest.size());
    }

    @Override
    public void removeAreaOfIntrest(int x, int y) {
        areaOfInterest.remove((Object) mosaikImageIndex(x, y)); // geht das besser?
        LOGGER.info("AreaOfInterest size = " + areaOfInterest.size());
    }

    @Override
    public void resetAreaOfIntrest() {
        areaOfInterest.clear();
    }

    @Override
    public BufferedImage getTile(int x, int y) {
        LOGGER.debug("getTile " + x + ", " + y);
        OriginalTile originalTile = image.getTile(mosaikImageIndex(x, y));
        boolean noDestinationTile = dstTilesList.isEmpty() || destinationImageIndexes.get(mosaikImageIndex(x, y)) == -1;
        return printDebugInformations((noDestinationTile) ? originalTile.getSrcImage() : originalTile.getComposedImage(), x, y);
    }

    private BufferedImage printDebugInformations(BufferedImage srcImage, int x, int y) {
        BufferedImage bufferedImage = new BufferedImage(srcImage.getWidth(), srcImage.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = (Graphics2D) bufferedImage.getGraphics();
        g2d.drawImage(srcImage, 0, 0, null);
        g2d.setColor(Color.red);
        g2d.drawString(x + ", " + y, 10, 10);
        g2d.drawString("" + areaOfInterest.contains(mosaikImageIndex(x, y)), 10, 25);
        return bufferedImage;
    }


    private int mosaikImageIndex(int x, int y) {
        LOGGER.trace("mosaikImageIndex from {},{}", x, y);
        return y * tilesPerRow.get() + x;
    }

    @Override
    public void generateMosaicImage() {

        if (image.getLength() == 0 || dstTilesList.size() == 0) return;
        LOGGER.info("generateMosaicImage");

        imageCalculated.set(false);
        // TODO muss das huer passieren auch wenn nur von linear zu random gewechselt wurde aber src und dswt gleich bleiben
        compareTiles(image.getTiles(), dstTilesList);

        image.unsetDstImages();


        destinationImageIndexes = ImageComposerFactory
                .getComposer(mode.get())
                .generate(tilesPerRow.get()
                        , tilesPerColumn.get()
                        , maxReuses.get()
                        , reuseDistance.get()
                        , areaOfInterest
                        , destinationTilesIDs);

        image.setOpacity(opacity.get());
        image.setPostColorAlignment(postColorAlignment.get());

        IntStream.range(0, destinationImageIndexes.size()).forEach(index -> {
            int dstTileID = destinationImageIndexes.get(index);
            if (dstTileID >= 0) {
                image.getTile(index).setDstImage(dstTilesList.get(dstTileID).getImage());
            }
        });

        imageCalculated.set(true);
    }

    @Override
    public String getDstTileInformation(int x, int y) {
        LOGGER.debug("getDstTileInformation {},{}", x, y);
        if (!dstTilesList.isEmpty() && destinationImageIndexes.get(mosaikImageIndex(x, y)) >= 0) {
            return dstTilesList.get(destinationImageIndexes.get(mosaikImageIndex(x, y))).getFilename();
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
    public BufferedImage getOriginalTile(int x, int y) {
        LOGGER.debug("getOriginalTile " + x + ", " + y);
        return printDebugInformations(image.getTile(mosaikImageIndex(x, y)).getSrcImage(), x, y);
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
