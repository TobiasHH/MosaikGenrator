package de.tobiashh.javafx;

import de.tobiashh.javafx.properties.PropertiesManager;
import de.tobiashh.javafx.save.ImageSaver;
import de.tobiashh.javafx.tiles.DstTile;
import de.tobiashh.javafx.tiles.OriginalTile;
import de.tobiashh.javafx.tools.ImageTools;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.stream.IntStream;

// TODO Methoden vernüftig benennen z.b. was heißt calculate / generate / ...
// TODO tests
// TODO BufferedImageSave implementieren
// TODO Step Feedback in der Statusbar implementieren
// TODO caching der scaled Tiles
// TODO Logger
// TODO bei Blur ist image schon geladen, daher die methode loadImage entsprechend umdesignen um beim Listener nur nötige sachen zu machen
// TODO Blur Mode sollte keine neuberechnung des Mosaics triggern
// TODO scrollpane / canvas nur ausschnitt berechnen (Zoom Problem)) durch canvas tiles mit z.b. 1000 x 1000 px
// TODO preColorAlignment implementieren
// TODO areaOfIntrest
// TODO Gibt es bessere Vergleichsalgorhytmen? die z.b. stärker auf kontruren / Details achten? z.b. kantenerkennung und diese kanten mit einbeziehen

public class MosaicImageModelImpl implements MosaicImageModel {
    public static final String[] FILE_EXTENSION = {"png", "jpg", "jpeg"};

    private final ReadOnlyIntegerWrapper dstTilesCount = new ReadOnlyIntegerWrapper();
    private final ReadOnlyObjectWrapper<BufferedImage> compositeImage = new ReadOnlyObjectWrapper<>();
    private final ReadOnlyObjectWrapper<BufferedImage> originalImage = new ReadOnlyObjectWrapper<>();
    private final ReadOnlyIntegerWrapper tilesPerColumn = new ReadOnlyIntegerWrapper();
    private final ReadOnlyIntegerWrapper dstTilesLoadProgress = new ReadOnlyIntegerWrapper();

    private final ObjectProperty<Path> srcImageFile = new SimpleObjectProperty<>();
    
    private OriginalTile[] mosaicImage;
    
    private final ObservableList<DstTile> dstTilesList = FXCollections.observableList(new ArrayList<>());

    private DstTilesLoaderTask task;

    public MosaicImageModelImpl() {
        super();
        initChangeListener();
        loadDstTiles(getDstTilesPath());
    }

    private void initChangeListener() {
        dstTilesLoadProgressProperty().addListener((observable, oldValue, newValue) -> dstTilesCount.set(newValue.intValue()));
        dstTilesList.addListener((ListChangeListener<DstTile>) change -> dstTilesCount.set(change.getList().size()));

        linearModeProperty().addListener((observable, oldValue, newValue) -> calculateMosaicImage());
        reuseDistanceProperty().addListener((observable, oldValue, newValue) -> calculateMosaicImage());
        maxReusesProperty().addListener((observable, oldValue, newValue) -> calculateMosaicImage());

        dstTilesPathProperty().addListener((observableValue, oldPath, newPath) -> loadDstTiles(newPath));
        scanSubFolderProperty().addListener(((observable, oldValue, newValue) -> loadDstTiles(getDstTilesPath())));

        opacityProperty().addListener((observable, oldValue, newValue) -> setOpacityInTiles(newValue.intValue()));
        postColorAlignmentProperty().addListener((observable, oldValue, newValue) -> setPostColorAlignmentInTiles(newValue.intValue()));

        srcImageFileProperty().addListener((observable, oldImageFile, newImageFile) -> loadImage(newImageFile));
        blurModeProperty().addListener((observable, oldValue, newValue) -> loadImage(getSrcImageFile()));
        tilesPerRowProperty().addListener((observable, oldValue, newValue) -> loadImage(getSrcImageFile()));
    }

    private void setPostColorAlignmentInTiles(int postColorAlignment) {
        for (OriginalTile originalTile : mosaicImage) {
            originalTile.setPostColorAlignment(postColorAlignment);
        }

        calculateCompositeImage();
    }

    private void setOpacityInTiles(int opacity) {
        for (OriginalTile originalTile : mosaicImage) {
            originalTile.setOpacity(opacity);
        }

        calculateCompositeImage();
    }

    private void loadDstTiles(Path newPath) {
        if (task != null) task.cancel(true);
        task = new DstTilesLoaderTask(newPath, isScanSubFolder(), getTileSize(), getCompareSize());
        task.progressProperty().addListener((observable, oldValue, newValue) -> dstTilesLoadProgress.set((int) (newValue.doubleValue() * 100)));
        task.setOnSucceeded(event -> {
           dstTilesList.clear();
           dstTilesList.addAll(task.getValue());
           calculateMosaicImage();
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private void loadImage(Path imageFile) {
        if(imageFile != null) {
            try {
                BufferedImage bufferedImage = ImageIO.read(imageFile.toFile());

                tilesPerColumn.set(Math.max(1, getTilesPerRow() * bufferedImage.getHeight() / bufferedImage.getWidth()));

                BufferedImage image = ImageTools.calculateScaledImage(bufferedImage,
                        getTilesPerRow() * getTileSize(),
                        getTilesPerColumn() * getTileSize(),
                        true);

                generateTiles((isBlurMode()?ImageTools.blurImage(image): image));
                calculateOriginalImage();
                calculateMosaicImage();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void generateTiles(BufferedImage image) {
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

    private void calculateOriginalImage() {
        BufferedImage returnValue = new BufferedImage(getTileSize() * getTilesPerRow(), getTileSize() * getTilesPerColumn(), BufferedImage.TYPE_INT_RGB);

        Graphics2D graphics = (Graphics2D) returnValue.getGraphics();

        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, returnValue.getWidth(), returnValue.getHeight());

        for (int y = 0; y < getTilesPerColumn(); y++) {
            for (int x = 0; x < getTilesPerRow(); x++) {
                OriginalTile originalTile = mosaicImage[index(x,y)];

                    graphics.drawImage(originalTile.getSrcImage(), x * getTileSize(), y * getTileSize(), null);         graphics.drawString(""+ x + "," + y + ":" + originalTile.getDstTileID(), x * getTileSize(), y * getTileSize() + graphics.getFontMetrics().getHeight());
                    graphics.drawString(""+ x + "," + y + ":ORIG", x * getTileSize(), y * getTileSize() + graphics.getFontMetrics().getHeight());
            }
        }

        originalImage.set(returnValue);
    }


    private void calculateCompositeImage() {
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
        return y * getTilesPerRow() + x;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean setDstTileIndex(OriginalTile actualTile, OriginalTile[] tiles)
    {
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
            if(getTileDistance(actualIndex, i) < getReuseDistance()) {
                tiles[i].addBlockedIds(tileID);
            }
        });

        return true;
    }

    @Override
    public void calculateMosaicImage() {
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

        calculateCompositeImage();
    }

    @Override
    public String getDstTileInformation(int x, int y) {
        if(!dstTilesList.isEmpty() && mosaicImage[index(x,y)].getDstTileID() >= 0) {return dstTilesList.get(mosaicImage[index(x,y)].getDstTileID()).getFilename();}
        return "";
    }

    @Override
    public void saveImage(Path path) {
        Thread thread = new Thread(new ImageSaver(path, mosaicImage, getTilesPerRow(), getTilesPerColumn(), getTileSize()));
        thread.setDaemon(true);
        thread.start();
    }

    private void generateDistinctRandomImage() {
       Arrays.stream(mosaicImage).forEach(OriginalTile::resetIndex);

        if (dstTilesList.size() > 0) {
            Random rand = new Random();

            int x;
            int y;

           while (Arrays.stream(mosaicImage).anyMatch(tile -> !tile.isIndexSet())) {

                 x = rand.nextInt(getTilesPerRow());
                y = rand.nextInt(getTilesPerColumn());

                if (!mosaicImage[index(x,y)].isIndexSet()) {
                    //
                    if (!setDstTileIndex(mosaicImage[index(x, y)], mosaicImage)) return;
                }
            }
        }
    }

    // How test is when it is private?
    protected int getTileDistance(int index1, int index2){
        return Math.abs(getTilePositionX(index1) - getTilePositionX(index2)) + Math.abs(getTilePositionY(index1) - getTilePositionY(index2));
    }

    private int getTilePositionX(int index) {
        return index % getTilesPerRow();
    }

    private int getTilePositionY(int index) {
        return index / getTilesPerRow();
    }

    private void generateDistinctLinearImage() {
        Arrays.stream(mosaicImage).forEach(OriginalTile::resetIndex);
        if (dstTilesList.size() > 0) {

            for (int y = 0; y < getTilesPerColumn(); y++) {
                for (int x = 0; x < getTilesPerRow(); x++) {
                    if (!setDstTileIndex(mosaicImage[index(x, y)], mosaicImage)) return;
                }
            }
        }
    }

    private void blockID(int id, OriginalTile tile, OriginalTile[] mosaicImage) {
        for (OriginalTile originalTile : mosaicImage) {
            if(originalTile != tile)
            {
                originalTile.addBlockedIds(id);
            }
        }
    }

    private ReadOnlyIntegerProperty dstTilesLoadProgressProperty() { return dstTilesLoadProgress.getReadOnlyProperty(); }

    private int getTilesPerColumn() {
        return tilesPerColumn.get();
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
    public BooleanProperty blurModeProperty() {
        return PropertiesManager.getInstance().blurModeProperty();
    }

    @Override
    public boolean isBlurMode() {
        return PropertiesManager.getInstance().isBlurMode();
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

    @Override
    public void deleteTile(int x, int y) {
        mosaicImage[index(x,y)].setDstTileIndex(-1);
        calculateCompositeImage();
    }
}
