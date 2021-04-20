package de.tobiashh.javafx;

import de.tobiashh.javafx.properties.PropertiesManager;
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

// TODO ImagePool für DST Images
// TODO DST Images besitzen  liste von Indexes wo sie genutzt sind
// TODO comparableImage aus tiles herauslösen
// TODO ImageComparator erstellen
// TODO Pair Klasse von tile und comparable image
// TODO ImageComparator hat jeweils 1 SrcTilePair und List preAdjusted dstTilesPairs
// TODO abklären wie sich garbage collection beim aufräumen verhält
// TODO scrollpane / canvas nur ausschnitt berechnen (Zoom Problem)) durch canvas tiles mit z.b. 1000 x 1000 px
// TODO preColorAlignment implementieren
// TODO Bilder ausschließen generell
// TODO Bild ausschließen an einer Position
// TODO Gibt es bessere Vergleichsalgorhytmen? die z.b. stärker auf kontruren / Details achten? z.b. kantenerkennung und diese kanten mit einbeziehen
// TODO tests

public class MosaicImageModelImpl implements MosaicImageModel {
    private final static Logger LOGGER = LoggerFactory.getLogger(MosaicImageModelImpl.class.getName());

    public static final String[] FILE_EXTENSION = {"png", "jpg", "jpeg"};

    private final ReadOnlyIntegerWrapper dstTilesCount = new ReadOnlyIntegerWrapper();
    private final ReadOnlyObjectWrapper<BufferedImage> compositeImage = new ReadOnlyObjectWrapper<>();
    private final ReadOnlyObjectWrapper<BufferedImage> originalImage = new ReadOnlyObjectWrapper<>();
    private final ReadOnlyIntegerWrapper tilesPerColumn = new ReadOnlyIntegerWrapper();
    private final ReadOnlyIntegerWrapper dstTilesLoadProgress = new ReadOnlyIntegerWrapper();
    private final ReadOnlyStringWrapper status = new ReadOnlyStringWrapper();

    private final ObjectProperty<Path> srcImageFile = new SimpleObjectProperty<>();
    
    private OriginalTile[] mosaicImage;
    private List<Integer> areaOfInterest = new ArrayList<>();
    
    private final ObservableList<DstTile> dstTilesList = FXCollections.observableList(new ArrayList<>());

    private DstTilesLoaderTask task;
    private TileDistance tileDistance;

    public MosaicImageModelImpl() {
        LOGGER.info("MosaicImageModelImpl");
        tileDistance = new TileDistance(getTilesPerRow());
        initChangeListener();
        loadDstTiles(getDstTilesPath());
    }

    private void initChangeListener() {
       LOGGER.info("initChangeListener");

        dstTilesLoadProgressProperty().addListener((observable, oldValue, newValue) -> dstTilesCount.set(newValue.intValue()));
        dstTilesList.addListener((ListChangeListener<DstTile>) change -> dstTilesCount.set(change.getList().size()));

        linearModeProperty().addListener((observable, oldValue, newValue) -> generateMosaicImage());
        reuseDistanceProperty().addListener((observable, oldValue, newValue) -> generateMosaicImage());
        maxReusesProperty().addListener((observable, oldValue, newValue) -> generateMosaicImage());

        dstTilesPathProperty().addListener((observableValue, oldPath, newPath) -> loadDstTiles(newPath));
        scanSubFolderProperty().addListener(((observable, oldValue, newValue) -> loadDstTiles(getDstTilesPath())));

        opacityProperty().addListener((observable, oldValue, newValue) -> setOpacityInTiles(newValue.intValue()));
        postColorAlignmentProperty().addListener((observable, oldValue, newValue) -> setPostColorAlignmentInTiles(newValue.intValue()));

        srcImageFileProperty().addListener((observable, oldImageFile, newImageFile) -> loadImage(newImageFile));
        tilesPerRowProperty().addListener((observable, oldValue, newValue) -> loadImage(getSrcImageFile()));
    }

    private void setPostColorAlignmentInTiles(int postColorAlignment) {
        LOGGER.info("setPostColorAlignmentInTiles {}%", postColorAlignment);
        for (OriginalTile originalTile : mosaicImage) {
            originalTile.setPostColorAlignment(postColorAlignment);
        }

        composeCompositeImage();
    }

    private void setOpacityInTiles(int opacity) {
        LOGGER.info("setOpacityInTiles {}%", opacity);
        for (OriginalTile originalTile : mosaicImage) {
            originalTile.setOpacity(opacity);
        }

        composeCompositeImage();
    }

    private void loadDstTiles(Path newPath) {
        LOGGER.info("loadDstTiles {}", newPath);
        if (task != null) task.cancel(true);
        task = new DstTilesLoaderTask(newPath, isScanSubFolder(), getTileSize(), getCompareSize());
        task.progressProperty().addListener((observable, oldValue, newValue) -> dstTilesLoadProgress.set((int) (newValue.doubleValue() * 100)));
        task.setOnSucceeded(event -> {
           dstTilesList.clear();
           dstTilesList.addAll(task.getValue());
           generateMosaicImage();
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private void loadImage(Path imageFile) {
        LOGGER.info("loadImage {}", imageFile);


        // longrunning operation runs on different thread
        Thread thread = new Thread(() -> {
            Platform.runLater(() -> status.set("Lade Bild"));
            if(imageFile != null) {
                try {
                    BufferedImage bufferedImage = ImageIO.read(imageFile.toFile());

                    tilesPerColumn.set(Math.max(1, getTilesPerRow() * bufferedImage.getHeight() / bufferedImage.getWidth()));

                    BufferedImage image = ImageTools.calculateScaledImage(bufferedImage,
                            getTilesPerRow() * getTileSize(),
                            getTilesPerColumn() * getTileSize(),
                            true);

                    calculateTiles(image);
                    composeOriginalImage();
                    generateMosaicImage();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Platform.runLater(() -> status.set("Bild geladen"));
        });
        // don't let thread prevent JVM shutdown
        thread.setDaemon(true);
        thread.start();
    }

    private void calculateTiles(BufferedImage image) {
        LOGGER.info("calculateTiles");
        mosaicImage = new OriginalTile[getTilesPerColumn() * getTilesPerRow()];
        for (int y = 0; y < getTilesPerColumn(); y++) {
            for (int x = 0; x < getTilesPerRow(); x++) {
                int tileSize = getTileSize();
                BufferedImage subImage = image.getSubimage(x * tileSize, y * tileSize, tileSize, tileSize);
                mosaicImage[index(x,y)] = new OriginalTile(subImage, getCompareSize());
            }
        }
    }

    private void compareTiles(OriginalTile[] mosaicImage, ObservableList<DstTile> dstTilesList)
    {
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
        if(!areaOfInterest.contains(index)) {
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

    private void composeOriginalImage() {
        LOGGER.info("composeOriginalImage");
        BufferedImage returnValue = new BufferedImage(getTileSize() * getTilesPerRow(), getTileSize() * getTilesPerColumn(), BufferedImage.TYPE_INT_RGB);

        Graphics2D graphics = (Graphics2D) returnValue.getGraphics();

        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, returnValue.getWidth(), returnValue.getHeight());

        for (int y = 0; y < getTilesPerColumn(); y++) {
            for (int x = 0; x < getTilesPerRow(); x++) {
                OriginalTile originalTile = mosaicImage[index(x,y)];
                    graphics.drawImage(originalTile.getSrcImage(), x * getTileSize(), y * getTileSize(), null);
                    graphics.drawString(""+ x + "," + y + ":" + originalTile.getDstTileID(), x * getTileSize(), y * getTileSize() + graphics.getFontMetrics().getHeight());
                    graphics.drawString(""+ x + "," + y + ":ORIG", x * getTileSize(), y * getTileSize() + graphics.getFontMetrics().getHeight() * 2);
            }
        }

        originalImage.set(returnValue);
    }

    // TODO double parameter ???
    /*
    public BufferedImage getComposedImagePart(double x, double y, double width, double height)
    {
        BufferedImage returnValue = new BufferedImage((int)width, (int)height, BufferedImage.TYPE_INT_RGB);
        System.out.println("x = " + x + ", y = " + y + ", width = " + width + ", height = " + height);

        Graphics2D graphics = (Graphics2D) returnValue.getGraphics();

        for (int yPos = 0; yPos < getTilesPerColumn(); yPos++) {
            for (int xPos = 0; xPos < getTilesPerRow(); xPos++) {
                OriginalTile originalTile = mosaicImage[index(xPos,yPos)];
                graphics.drawImage(originalTile.getSrcImage(), (int) (xPos * getTileSize() - x), (int) (yPos * getTileSize() - y), null);
                graphics.drawString(""+ xPos + "," + yPos + ":" + originalTile.getDstTileID(), xPos * getTileSize(), yPos * getTileSize() + graphics.getFontMetrics().getHeight());
                graphics.drawString(""+ xPos + "," + yPos + ":ORIG", xPos * getTileSize(), yPos * getTileSize() + graphics.getFontMetrics().getHeight() * 2);
            }
        }

        return returnValue;
    }

     */

    private void composeCompositeImage() {
        LOGGER.info("composeCompositeImage");
        BufferedImage returnValue = new BufferedImage(getTileSize() * getTilesPerRow(), getTileSize() * getTilesPerColumn(), BufferedImage.TYPE_INT_RGB);

        Graphics2D graphics = (Graphics2D) returnValue.getGraphics();

        graphics.setColor(Color.GREEN);
        graphics.fillRect(0, 0, returnValue.getWidth(), returnValue.getHeight());

        for (int y = 0; y < getTilesPerColumn(); y++) {
            for (int x = 0; x < getTilesPerRow(); x++) {
                OriginalTile originalTile = mosaicImage[index(x,y)];
                if (originalTile.getDstTileID()>= 0) {
                    DstTile dstTile = dstTilesList.get(originalTile.getDstTileID());
                    graphics.drawImage(originalTile.getComposedImage(), x * getTileSize(), y * getTileSize(), null);
                    graphics.drawString("x:"+ x + " y:" + y + " ID:" + originalTile.getDstTileID(), x * getTileSize(), y * getTileSize() + graphics.getFontMetrics().getHeight());
                    graphics.drawString("index:" + originalTile.getDstTileIndex(), x * getTileSize(), y * getTileSize() + graphics.getFontMetrics().getHeight() * 2);
                    graphics.drawString("fn:" + dstTile.getFilename(), x * getTileSize(), y * getTileSize() + graphics.getFontMetrics().getHeight() * 3);
                } else {
                    graphics.drawImage(originalTile.getSrcImage(), x * getTileSize(), y * getTileSize(), null);
                     graphics.drawString(""+ x + "," + y + ":ORIG", x * getTileSize(), y * getTileSize() + graphics.getFontMetrics().getHeight());
                }
            }
        }

        compositeImage.set(returnValue);
    }

    private int index(int x, int y)
    {
        LOGGER.trace("index from {},{}", x, y);
        return y * getTilesPerRow() + x;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean setDstTileIndex(OriginalTile actualTile, OriginalTile[] tiles)
    {
        LOGGER.debug("setDstTileIndex");
        if(actualTile.getDstTileIndex() > -1) return false;

        actualTile.incrementDstTileIndex();

        if(actualTile.getDstTileIndex() == -1) return false;

        int tileID = actualTile.getDstTileID();

        if(Arrays.stream(mosaicImage).filter(t -> t.getDstTileID() == tileID).count() > getMaxReuses())
        {
            blockID(tileID, actualTile, tiles);
        }

        int actualIndex = IntStream.range(0, tiles.length).filter(i -> tiles[i] == actualTile).findFirst().orElse(-1);
        IntStream.range(0, tiles.length).forEach(i -> {
            if(tileDistance.calculate(actualIndex, i) < getReuseDistance()) {
                tiles[i].addBlockedIds(tileID);
            }
        });

        return true;
    }

    @Override
    public void generateMosaicImage() {
        LOGGER.info("generateMosaicImage");
        compareTiles(mosaicImage, dstTilesList);

        for (OriginalTile originalTile : mosaicImage) {
            originalTile.setDstImage(null);
        }

        if (isLinearMode()) {
            generateDistinctLinearImage();
        } else {
            generateDistinctRandomImage();
        }

        for (OriginalTile originalTile : mosaicImage) {
            int dstTileID = originalTile.getDstTileID();
            if(dstTileID >= 0) {
                originalTile.setOpacity(getOpacity());
                originalTile.setPostColorAlignment(getPostColorAlignment());
                originalTile.setDstImage(dstTilesList.get(dstTileID).getImage());
            }
        }

        composeCompositeImage();
    }

    @Override
    public String getDstTileInformation(int x, int y) {
        LOGGER.debug("getDstTileInformation {},{}", x, y);
        if(!dstTilesList.isEmpty() && mosaicImage[index(x,y)].getDstTileID() >= 0) {return dstTilesList.get(mosaicImage[index(x,y)].getDstTileID()).getFilename();}
        return "";
    }

    @Override
    public void saveMosaicImage(Path path) {
        LOGGER.info("saveMosaicImage {}", path);
        Thread thread = new Thread(new ImageSaver(path, mosaicImage, getTilesPerRow(), getTilesPerColumn(), getTileSize()));
        thread.setDaemon(true);
        thread.start();
    }

    private void generateDistinctRandomImage() {
        LOGGER.info("generateDistinctRandomImage");
       Arrays.stream(mosaicImage).forEach(OriginalTile::resetIndex);

        if (dstTilesList.size() > 0) {
            Random rand = new Random();

            int tilesCount = getTilesPerColumn() * getTilesPerRow();

            List<Integer> tileIndices = IntStream.range(0, tilesCount).boxed().collect(Collectors.toList());
            List<Integer> areaOfIntrestIndices = new ArrayList<>(areaOfInterest);
            tileIndices.removeAll(areaOfIntrestIndices);

            while(areaOfIntrestIndices.size() > 0)
            {
                if (!setDstTileIndex(mosaicImage[areaOfIntrestIndices.remove(rand.nextInt(areaOfIntrestIndices.size()))], mosaicImage)) return;
            }

            while(tileIndices.size() > 0)
            {
                if (!setDstTileIndex(mosaicImage[tileIndices.remove(rand.nextInt(tileIndices.size()))], mosaicImage)) return;
            }
        }
    }

    private void generateDistinctLinearImage() {
        LOGGER.info("generateDistinctLinearImage");
        Arrays.stream(mosaicImage).forEach(OriginalTile::resetIndex);
        if (dstTilesList.size() > 0) {

            for (int y = 0; y < getTilesPerColumn(); y++) {
                for (int x = 0; x < getTilesPerRow(); x++) {
                    if(areaOfInterest.contains(index(x,y))){
                        if (!setDstTileIndex(mosaicImage[index(x, y)], mosaicImage)) return;
                    }
                }
            }

            for (int y = 0; y < getTilesPerColumn(); y++) {
                for (int x = 0; x < getTilesPerRow(); x++) {
                    if(!areaOfInterest.contains(index(x,y))) {
                        if (!setDstTileIndex(mosaicImage[index(x, y)], mosaicImage)) return;
                    }
                }
            }
        }
    }

    private void blockID(int id, OriginalTile tile, OriginalTile[] mosaicImage) {
        LOGGER.debug("blockID");
        for (OriginalTile originalTile : mosaicImage) {
            if(originalTile != tile)
            {
                originalTile.addBlockedIds(id);
            }
        }
    }

    private ReadOnlyIntegerProperty dstTilesLoadProgressProperty() { return dstTilesLoadProgress.getReadOnlyProperty(); }

    @Override
    public int getTilesPerColumn() {
        return tilesPerColumn.get();
    }

    @Override
    public ReadOnlyStringWrapper statusProperty() {
        return status;
    }

    @Override
    public ReadOnlyObjectProperty<BufferedImage> compositeImageProperty() {
        return compositeImage.getReadOnlyProperty();
    }

    @Override
    public BufferedImage getCompositeImage() {
        return compositeImage.get();
    }

    @Override
    public BufferedImage getOriginalImage() {
        return originalImage.get();
    }

    @Override
    public ReadOnlyIntegerProperty dstTilesCountProperty() {
        return dstTilesCount.getReadOnlyProperty();
    }

    @Override
    public BooleanProperty linearModeProperty() {
        return PropertiesManager.getInstance().linearModeProperty();
    }

    @Override
    public boolean isLinearMode() {
        return PropertiesManager.getInstance().isLinearMode();
    }

    @Override
    public int getTileSize() {
        return PropertiesManager.getInstance().getTileSize();
    }

    @Override
    public int getCompareSize() {
        return PropertiesManager.getInstance().getCompareSize();
    }

    @Override
    public IntegerProperty opacityProperty() {
        return PropertiesManager.getInstance().opacityProperty();
    }

    @Override
    public int getOpacity() {
        return PropertiesManager.getInstance().getOpacity();
    }

    @Override
    public void setOpacity(int opacity) {
        PropertiesManager.getInstance().setOpacity(opacity);
    }

    @Override
    public IntegerProperty preColorAlignmentProperty() {
        return PropertiesManager.getInstance().preColorAlignmentProperty();
    }

    @Override
    public int getPreColorAlignment() {
        return PropertiesManager.getInstance().getPreColorAlignment();
    }

    @Override
    public void setPreColorAlignment(int preColorAlignment) {
        PropertiesManager.getInstance().setPreColorAlignment(preColorAlignment);
    }

    @Override
    public IntegerProperty postColorAlignmentProperty() {
        return PropertiesManager.getInstance().postColorAlignmentProperty();
    }

    @Override
    public int getPostColorAlignment() {
        return PropertiesManager.getInstance().getPostColorAlignment();
    }

    @Override
    public void setPostColorAlignment(int postColorAlignment) {
        PropertiesManager.getInstance().setPostColorAlignment(postColorAlignment);
    }

    @Override
    public IntegerProperty maxReusesProperty() {
        return PropertiesManager.getInstance().maxReusesProperty();
    }

    @Override
    public int getMaxReuses() {
        return PropertiesManager.getInstance().getMaxReuses();
    }

    @Override
    public void setMaxReuses(int maxReuses) {
        PropertiesManager.getInstance().setMaxReuses(maxReuses);
    }

    @Override
    public IntegerProperty reuseDistanceProperty() {
        return PropertiesManager.getInstance().reuseDistanceProperty();
    }

    @Override
    public int getReuseDistance() {
        return PropertiesManager.getInstance().getReuseDistance();
    }

    @Override
    public void setReuseDistance(int reuseDistance) {
        PropertiesManager.getInstance().setReuseDistance(reuseDistance);
    }

    @Override
    public BooleanProperty scanSubFolderProperty() {
        return PropertiesManager.getInstance().scanSubFolderProperty();
    }

    @Override
    public boolean isScanSubFolder() {
        return PropertiesManager.getInstance().isScanSubFolder();
    }

    @Override
    public ObjectProperty<Path> srcImageFileProperty() { return srcImageFile; }

    @Override
    public Path getSrcImageFile() {
        return srcImageFile.get();
    }

    @Override
    public void setSrcImageFile(Path file) {
        srcImageFile.set(file);
    }

    @Override
    public ObjectProperty<Path> dstTilesPathProperty() {
        return PropertiesManager.getInstance().tilesPathProperty();
    }

    @Override
    public Path getDstTilesPath() {
        return PropertiesManager.getInstance().getTilesPath();
    }

    @Override
    public void setDstTilesPath(Path dstTilesPath) {
        PropertiesManager.getInstance().setTilesPath(dstTilesPath);
    }

    @Override
    public IntegerProperty tilesPerRowProperty() {
        return PropertiesManager.getInstance().tilesPerRowProperty();
    }

    @Override
    public int getTilesPerRow() {
        return PropertiesManager.getInstance().getTilesPerRow();
    }

    @Override
    public void setTilesPerRow(int count) {
        PropertiesManager.getInstance().setTilesPerRow(count);
    }
}
