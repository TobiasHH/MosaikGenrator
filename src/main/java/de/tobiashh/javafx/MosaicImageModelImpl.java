package de.tobiashh.javafx;

import de.tobiashh.javafx.properties.PropertiesManager;
import de.tobiashh.javafx.save.ImageSaver;
import de.tobiashh.javafx.tiles.MosaicTile;
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

// TODO Pfad kann in Properties nciht gesetzt sein, führt z ueiner Exception
// TODO Naming srcImage / srcTiles / dstTiles ...
// TODO zugriff auf Properties immer über get set Property methode
// TODO private und public prüfen
// TODO Methoden vernüftig benennen z.b. was heißt calculate / generate / ...
// TODO tests
// TODO save function / BackgroundImage
// TODO Step Feedback in der Statusbar implementieren
// TODO caching der scaled Tiles
// TODO Logger
// TODO bei Blur ist image schon geladen, daher die methode loadImage entsprechend umdesignen um beim Listener nur nötige sachen zu machen
// TODO Blur Mode sollte keine neuberechnung des Mosaics triggern
// TODO scrollpane / canvas nur ausschnitt berechnen (Zoom Problem))
// TODO preColorAlignment implementieren
// TODO areaOfIntrest
// TODO Gibt es bessere Vergleichsalgorhytmen? die z.b. stärker auf kontruren / Details achten? z.b. kantenerkennung und diese kanten mit einbeziehen

public class MosaicImageModelImpl implements MosaicImageModel {
    public static final String[] FILE_EXTENSION = {"png", "jpg", "jpeg"};

    private final ReadOnlyIntegerWrapper dstTilesCount = new ReadOnlyIntegerWrapper();
    private final ReadOnlyObjectWrapper<BufferedImage> compositeImage = new ReadOnlyObjectWrapper<>();
    private final ReadOnlyObjectWrapper<BufferedImage> originalImage = new ReadOnlyObjectWrapper<>();
    private final ReadOnlyIntegerWrapper tilesPerColumn = new ReadOnlyIntegerWrapper();
    private final ReadOnlyIntegerWrapper mosaicLoadProgress = new ReadOnlyIntegerWrapper();

    private final ObjectProperty<Path> imageFile = new SimpleObjectProperty<>();
    
    private OriginalTile[] mosaicImage;
    
    ObservableList<MosaicTile> mosaicTilesList = FXCollections.observableList(new ArrayList<>());

    private MosaicTilesLoaderTask task;

    public MosaicImageModelImpl() {
        super();
        initChangeListener();
        loadMosaicTiles(getMosaicTilesPath());
    }

    private void initChangeListener() {
        mosaicLoadProgress.addListener((observable, oldValue, newValue) -> dstTilesCount.set(newValue.intValue()));
        mosaicTilesList.addListener((ListChangeListener<MosaicTile>) change -> dstTilesCount.set(change.getList().size()));

        linearModeProperty().addListener((observable, oldValue, newValue) -> calculateMosaicImage());

        mosaicTilesPathProperty().addListener((observableValue, oldPath, newPath) -> loadMosaicTiles(newPath));
        scanSubFolderProperty().addListener(((observable, oldValue, newValue) -> loadMosaicTiles(getMosaicTilesPath())));

        opacityProperty().addListener((observable, oldValue, newValue) -> setOpacityInTiles(newValue.intValue()));
        postColorAlignmentProperty().addListener((observable, oldValue, newValue) -> setPostColorAlignmentInTiles(newValue.intValue()));

        imageFile.addListener((observable, oldImageFile, newImageFile) -> loadImage(newImageFile));
        blurModeProperty().addListener((observable, oldValue, newValue) -> loadImage(getImageFile()));
        tilesPerRowProperty().addListener((observable, oldValue, newValue) -> loadImage(getImageFile()));
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

    private void loadMosaicTiles(Path newPath) {
        if (task != null) task.cancel(true);
        task = new MosaicTilesLoaderTask(newPath, isScanSubFolder(), getTileSize());
        task.progressProperty().addListener((observable, oldValue, newValue) -> mosaicLoadProgress.set((int) (newValue.doubleValue() * 100)));
        task.setOnSucceeded(event -> {
           mosaicTilesList.clear();
           mosaicTilesList.addAll(task.getValue());
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
                mosaicImage[index(x,y)] = new OriginalTile(subImage);
            }
        }
    }

    public void compareTiles(OriginalTile[] mosaicImage, ObservableList<MosaicTile> mosaicTilesList)
    {
         for (OriginalTile originalTile : mosaicImage) {
            Map<Integer, Integer> scores = new HashMap<>();

            for (int i = 0; i < mosaicTilesList.size(); i++) {
                scores.put(i, originalTile.compare(mosaicTilesList.get(i)));
            }

            List<Map.Entry<Integer, Integer>> list = new ArrayList<>(scores.entrySet());
            list.sort(Map.Entry.comparingByValue());
            int[] keys = list.stream().mapToInt(Map.Entry::getKey).toArray();
            originalTile.setMosaicTileIDs(keys);
        }
    }

    private void calculateOriginalImage() {
        BufferedImage returnValue = new BufferedImage(getTileSize() * getTilesPerRow(), getTileSize() * getTilesPerColumn(), BufferedImage.TYPE_INT_RGB);

        Graphics2D graphics = (Graphics2D) returnValue.getGraphics();

        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, returnValue.getWidth(), returnValue.getHeight());

        for (int y = 0; y < getTilesPerColumn(); y++) {
            for (int x = 0; x < getTilesPerRow(); x++) {
                OriginalTile mosaicTile = mosaicImage[index(x,y)];

                    graphics.drawImage(mosaicTile.getSrcImage(), x * getTileSize(), y * getTileSize(), null);         graphics.drawString(""+ x + "," + y + ":" + mosaicTile.getMosaicTileID(), x * getTileSize(), y * getTileSize() + graphics.getFontMetrics().getHeight());
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
                if (originalTile.getMosaicTileID()>= 0) {
                    MosaicTile mosaicTile = mosaicTilesList.get(originalTile.getMosaicTileID());
                    graphics.drawImage(originalTile.getComposedImage(), x * getTileSize(), y * getTileSize(), null);
                    graphics.drawString("x:"+ x + " y:" + y + " ID:" + originalTile.getMosaicTileID(), x * getTileSize(), y * getTileSize() + graphics.getFontMetrics().getHeight());
                    graphics.drawString("index:" + originalTile.getMosaicTileIndex(), x * getTileSize(), y * getTileSize() + graphics.getFontMetrics().getHeight() * 2);
                    graphics.drawString("fn:" + mosaicTile.getFilename(), x * getTileSize(), y * getTileSize() + graphics.getFontMetrics().getHeight() * 3);
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
    private boolean setMosaicTileIndex(OriginalTile actualTile, OriginalTile[] tiles)
    {
        if(actualTile.getMosaicTileIndex() > -1) return false;

        actualTile.incrementMosaicTileIndex();

        if(actualTile.getMosaicTileIndex() == -1) return false;

        int tileID = actualTile.getMosaicTileID();

        if(Arrays.stream(mosaicImage).filter(t -> t.getMosaicTileID() == tileID).count() > getMaxReuses())
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

    public void calculateMosaicImage() {
        compareTiles(mosaicImage, mosaicTilesList);

        for (OriginalTile originalTile : mosaicImage) {
            originalTile.setDstImage(null);
        }

        if (isLinearMode()) {
            generateDistinctLinearImage();
        } else {
            generateDistinctRandomImage();
        }

        for (OriginalTile originalTile : mosaicImage) {
            int mosaicTileID = originalTile.getMosaicTileID();
            if(mosaicTileID >= 0) {
                originalTile.setOpacity(getOpacity());
                originalTile.setPostColorAlignment(getPostColorAlignment());
                originalTile.setDstImage(mosaicTilesList.get(mosaicTileID).getImage());
            }
        }

        calculateCompositeImage();
    }

    @Override
    public String getMosaicTileInformation(int x, int y) {
        if(!mosaicTilesList.isEmpty() && mosaicImage[index(x,y)].getMosaicTileID() >= 0) {return mosaicTilesList.get(mosaicImage[index(x,y)].getMosaicTileID()).getFilename();}
        return "";
    }

    @Override
    public void saveImage() {
        Thread thread = new Thread(new ImageSaver(Path.of("test.png"), mosaicImage, getTilesPerRow(), getTilesPerColumn(), getTileSize()));
        thread.setDaemon(true);
        thread.start();
    }

    private void generateDistinctRandomImage() {
       Arrays.stream(mosaicImage).forEach(OriginalTile::resetIndex);

        if (mosaicTilesList.size() > 0) {
            Random rand = new Random();

            int x;
            int y;

           while (Arrays.stream(mosaicImage).anyMatch(tile -> !tile.isIndexSet())) {

                 x = rand.nextInt(getTilesPerRow());
                y = rand.nextInt(getTilesPerColumn());

                if (!mosaicImage[index(x,y)].isIndexSet()) {
                    //
                    if (!setMosaicTileIndex(mosaicImage[index(x, y)], mosaicImage)) return;
                }
            }
        }
    }

    public int getTileDistance(int index1, int index2){
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
        if (mosaicTilesList.size() > 0) {

            for (int y = 0; y < getTilesPerColumn(); y++) {
                for (int x = 0; x < getTilesPerRow(); x++) {
                    if (!setMosaicTileIndex(mosaicImage[index(x, y)], mosaicImage)) return;
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

    public int getTilesPerColumn() {
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
    public Path getImageFile() {
        return imageFile.get();
    }

    @Override
    public void setImageFile(Path file) {
        imageFile.set(file);
    }

    @Override
    public ObjectProperty<Path> mosaicTilesPathProperty() {
        return PropertiesManager.getInstance().tilesPathProperty();
    }

    @Override
    public Path getMosaicTilesPath() {
        return PropertiesManager.getInstance().getTilesPath();
    }

    @Override
    public void setMosaicTilesPath(Path mosaicTilesPath) {
        PropertiesManager.getInstance().setTilesPath(mosaicTilesPath);
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
        mosaicImage[index(x,y)].setMosaicTileIndex(-1);
        calculateCompositeImage();
    }
}
