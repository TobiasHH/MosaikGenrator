package de.tobiashh.javafx;

import de.tobiashh.javafx.properties.Properties;
import de.tobiashh.javafx.save.ImageSaver;
import de.tobiashh.javafx.tiles.MosaikTile;
import de.tobiashh.javafx.tiles.OriginalTile;
import de.tobiashh.javafx.tools.ImageTools;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.List;

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
// TODO Blur Mode sollte keine neuberechnung des Mosaiks triggern
// TODO scrollpane / canvas nur ausschnitt berechnen (Zoom Problem))
// TODO reuse
// TODO preColorAlignment implementieren
// TODO areOfIntrest
// TODO Gibt es bessere Vergleichsalgorhytmen? die z.b. stärker auf kontruren / Details achten? z.b. kantenerkennung und diese kanten mit einbeziehen

public class MosaikImageModelImpl implements MosaikImageModel {
    public static final String[] FILE_EXTENSION = {"png", "jpg", "jpeg"};

    private final ReadOnlyIntegerWrapper dstTilesCount = new ReadOnlyIntegerWrapper();
    private final ReadOnlyObjectWrapper<BufferedImage> compositeImage = new ReadOnlyObjectWrapper<>();
    private final ReadOnlyObjectWrapper<BufferedImage> originalImage = new ReadOnlyObjectWrapper<>();
    private final ReadOnlyIntegerWrapper tilesY = new ReadOnlyIntegerWrapper();
    private final ReadOnlyIntegerWrapper mosaikLoadProgress = new ReadOnlyIntegerWrapper();

    private final ObjectProperty<Path> imageFile = new SimpleObjectProperty<>();
    
    private OriginalTile[] mosaikImage;
    
    ObservableList<MosaikTile> mosaikTilesList = FXCollections.observableList(new ArrayList<>());

    private MosaikTilesLoaderTask task;

    public MosaikImageModelImpl() {
        super();
        initChangeListener();
        loadMosaikTiles(getMosaikTilesPath());
    }

    private void initChangeListener() {
        mosaikLoadProgress.addListener((observable, oldValue, newValue) -> dstTilesCount.set(newValue.intValue()));
        mosaikTilesList.addListener((ListChangeListener<MosaikTile>) change -> dstTilesCount.set(change.getList().size()));

        linearModeProperty().addListener((observable, oldValue, newValue) -> calculateMosaikImage());

        mosaikTilesPathProperty().addListener((observableValue, oldPath, newPath) -> loadMosaikTiles(newPath));
        scanSubFolderProperty().addListener(((observable, oldValue, newValue) -> loadMosaikTiles(getMosaikTilesPath())));

        opacityProperty().addListener((observable, oldValue, newValue) -> setOpacityInTiles(newValue.intValue()));
        postColorAlignmentProperty().addListener((observable, oldValue, newValue) -> setPostColorAlignmentInTiles(newValue.intValue()));

        imageFile.addListener((observable, oldImageFile, newImageFile) -> loadImage(newImageFile));
        blurModeProperty().addListener((observable, oldValue, newValue) -> loadImage(getImageFile()));
        tilesXProperty().addListener((observable, oldValue, newValue) -> loadImage(getImageFile()));
    }

    private void setPostColorAlignmentInTiles(int postColorAlignment) {
        for (OriginalTile originalTile : mosaikImage) {
            originalTile.setPostColorAlignment(postColorAlignment);
        }

        calculateCompositeImage();
    }

    private void setOpacityInTiles(int opacity) {
        for (OriginalTile originalTile : mosaikImage) {
            originalTile.setOpacity(opacity);
        }

        calculateCompositeImage();
    }

    private void loadMosaikTiles(Path newPath) {
        if (task != null) task.cancel(true);
        task = new MosaikTilesLoaderTask(newPath, isScanSubFolder(), getTileSize());
        task.progressProperty().addListener((observable, oldValue, newValue) -> mosaikLoadProgress.set((int) (newValue.doubleValue() * 100)));
        task.setOnSucceeded(event -> {
           mosaikTilesList.clear();
           mosaikTilesList.addAll(task.getValue());
           calculateMosaikImage();
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private void loadImage(Path imageFile) {
        if(imageFile != null) {
            try {
                BufferedImage bufferedImage = ImageIO.read(imageFile.toFile());

                tilesY.set(Math.max(1, getTilesX() * bufferedImage.getHeight() / bufferedImage.getWidth()));

                BufferedImage image = ImageTools.calculateScaledImage(bufferedImage,
                        getTilesX() * getTileSize(),
                        getTilesY() * getTileSize(),
                        true);

                generateTiles((isBlurMode()?ImageTools.blurImage(image): image));
                calculateOriginalImage();
                calculateMosaikImage();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void generateTiles(BufferedImage image) {
        mosaikImage = new OriginalTile[getTilesY() * getTilesX()];
        for (int y = 0; y < getTilesY(); y++) {
            for (int x = 0; x < getTilesX(); x++) {
                int tileSize = getTileSize();
                BufferedImage subImage = image.getSubimage(x * tileSize, y * tileSize, tileSize, tileSize);
                mosaikImage[index(x,y)] = new OriginalTile(subImage);
            }
        }
    }

    public void compareTiles(OriginalTile[] mosaikImage, ObservableList<MosaikTile> mosaikTilesList)
    {
         for (OriginalTile originalTile : mosaikImage) {
            Map<Integer, Integer> scores = new HashMap<>();

            for (int i = 0; i < mosaikTilesList.size(); i++) {
                scores.put(i, originalTile.compare(mosaikTilesList.get(i)));
            }

            List<Map.Entry<Integer, Integer>> list = new ArrayList<>(scores.entrySet());
            list.sort(Map.Entry.comparingByValue());
            int[] keys = list.stream().mapToInt(Map.Entry::getKey).toArray();
            originalTile.setMosikTileIDs(keys);
        }
    }

    private void calculateOriginalImage() {
        BufferedImage retval = new BufferedImage(getTileSize() * getTilesX(), getTileSize() * getTilesY(), BufferedImage.TYPE_INT_RGB);

        Graphics2D graphics = (Graphics2D) retval.getGraphics();

        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, retval.getWidth(), retval.getHeight());

        for (int y = 0; y < getTilesY(); y++) {
            for (int x = 0; x < getTilesX(); x++) {
                OriginalTile mosaikTile = mosaikImage[index(x,y)];

                    graphics.drawImage(mosaikTile.getSrcImage(), x * getTileSize(), y * getTileSize(), null);         graphics.drawString(""+ x + "," + y + ":" + mosaikTile.getMosaikTileID(), x * getTileSize(), y * getTileSize() + graphics.getFontMetrics().getHeight());
                    graphics.drawString(""+ x + "," + y + ":ORIG", x * getTileSize(), y * getTileSize() + graphics.getFontMetrics().getHeight());
            }
        }

        originalImage.set(retval);
    }


    private void calculateCompositeImage() {
        BufferedImage retval = new BufferedImage(getTileSize() * getTilesX(), getTileSize() * getTilesY(), BufferedImage.TYPE_INT_RGB);

        Graphics2D graphics = (Graphics2D) retval.getGraphics();

        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, retval.getWidth(), retval.getHeight());

        for (int y = 0; y < getTilesY(); y++) {
            for (int x = 0; x < getTilesX(); x++) {
                OriginalTile originalTile = mosaikImage[index(x,y)];
                if (originalTile.getMosaikTileID()>= 0) {
                    MosaikTile mosaikTile = mosaikTilesList.get(originalTile.getMosaikTileID());
                    graphics.drawImage(originalTile.getComposedImage(), x * getTileSize(), y * getTileSize(), null);
                    graphics.drawString(""+ x + "," + y + ":" + originalTile.getMosaikTileID(), x * getTileSize(), y * getTileSize() + graphics.getFontMetrics().getHeight());
                    graphics.drawString("index:" + originalTile.getMosikTileIndex(), x * getTileSize(), y * getTileSize() + graphics.getFontMetrics().getHeight() * 2);
                    graphics.drawString("fn:" + mosaikTile.getFilename(), x * getTileSize(), y * getTileSize() + graphics.getFontMetrics().getHeight() * 3);
                } else {
                    graphics.drawImage(originalTile.getSrcImage(), x * getTileSize(), y * getTileSize(), null);
                     graphics.drawString(""+ x + "," + y + ":ORIG", x * getTileSize(), y * getTileSize() + graphics.getFontMetrics().getHeight());
                }
            }
        }

        compositeImage.set(retval);
    }

    private int index(int x, int y)
    {
        return y * getTilesX() + x;
    }
    
    private int getDistinctTileID(OriginalTile actualTile, OriginalTile[] tiles)
    {
        boolean notDistinct = true;

        while(notDistinct)
        {
            notDistinct = false;
            if(actualTile.getMosikTileIndex() == -1) actualTile.incrementMosaikTileIndex();
            int actualID = actualTile.getMosaikTileID();

            for (OriginalTile tile : tiles) {
                if(actualTile != tile) {
                    if (tile.getMosaikTileID() == actualID) {
                         notDistinct = true;
                        if (!actualTile.incrementMosaikTileIndex()) {
                              return actualTile.getMosikTileIndex();
                        }
                        break;
                    }
                }
            }
        }

         return actualTile.getMosikTileIndex();
    }

    public void calculateMosaikImage() {
        compareTiles(mosaikImage, mosaikTilesList);

        for (OriginalTile originalTile : mosaikImage) {
            originalTile.setDstImage(null);
        }

        if (isLinearMode()) {
            generateDistinctLinearImage();
        } else {
            generateDistinctRandomImage();
        }

        for (OriginalTile originalTile : mosaikImage) {
            int mosaikTileID = originalTile.getMosaikTileID();
            if(mosaikTileID >= 0) {
                originalTile.setOpacity(getOpacity());
                originalTile.setPostColorAlignment(getPostColorAlignment());
                originalTile.setDstImage(mosaikTilesList.get(mosaikTileID).getImage());
            }
        }

        calculateCompositeImage();
    }

    @Override
    public String getMosaikTileInformation(int x, int y) {
        if(!mosaikTilesList.isEmpty() && mosaikImage[index(x,y)].getMosaikTileID() >= 0) {return mosaikTilesList.get(mosaikImage[index(x,y)].getMosaikTileID()).getFilename();}
        return "";
    }

    @Override
    public void saveImage() {
        Thread thread = new Thread(new ImageSaver(Path.of("test.png"), mosaikImage, getTilesX(), getTilesY(), getTileSize()));
        thread.setDaemon(true);
        thread.start();
    }

    private void generateDistinctRandomImage() {
       Arrays.stream(mosaikImage).forEach(OriginalTile::resetIndex);

        if (mosaikTilesList.size() > 0) {
            Random rand = new Random();

            int x;
            int y;

           while (Arrays.stream(mosaikImage).anyMatch(tile -> !tile.isIndexSet())) {

                 x = rand.nextInt(getTilesX());
                y = rand.nextInt(getTilesY());

                if (!mosaikImage[index(x,y)].isIndexSet()) {
                    if (setDistinctTile(mosaikImage[index(x, y)])) return;
                }
            }
        }
    }

    private void generateDistinctLinearImage() {
        Arrays.stream(mosaikImage).forEach(OriginalTile::resetIndex);
        if (mosaikTilesList.size() > 0) {

            for (int y = 0; y < getTilesY(); y++) {
                for (int x = 0; x < getTilesX(); x++) {
                    if (setDistinctTile(mosaikImage[index(x, y)])) return;
                }
            }
        }
    }

    private boolean setDistinctTile(OriginalTile tile) {
        int tileIndex = getDistinctTileID(tile, mosaikImage);
        if (tileIndex == -1) return true;
        tile.setMosaikTileIndex(tileIndex);
        int tileID = tile.getMosaikTileID();
        blockID(tileID, tile, mosaikImage);
        return false;
    }

    private void blockID(int id, OriginalTile tile, OriginalTile[] mosaikImage) {
        for (OriginalTile originalTile : mosaikImage) {
            if(originalTile != tile)
            {
                originalTile.addBlockedIds(id);
            }
        }
    }

    public ReadOnlyIntegerProperty tilesYProperty() {
        return tilesY.getReadOnlyProperty();
    }

    public int getTilesY() {
        return tilesY.get();
    }

    public ReadOnlyIntegerProperty mosaikLoadProgressProperty() {
        return mosaikLoadProgress.getReadOnlyProperty();
    }

    public double getMosaikLoadProgress() {
        return mosaikLoadProgress.get();
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
    public ReadOnlyObjectProperty<BufferedImage> originalImageProperty() {
        return originalImage.getReadOnlyProperty();
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
    public int getDstTilesCount() {
        return dstTilesCount.get();
    }

    @Override
    public BooleanProperty linearModeProperty() {
        return Properties.getInstance().linearModeProperty();
    }

    @Override
    public boolean isLinearMode() {
        return Properties.getInstance().isLinearMode();
    }

    @Override
    public void setLinearMode(boolean linearMode) {
        Properties.getInstance().setLinearMode(linearMode);
    }

    @Override
    public IntegerProperty tileSizeProperty() {
        return Properties.getInstance().tileSizeProperty();
    }

    @Override
    public int getTileSize() {
        return Properties.getInstance().getTileSize();
    }

    @Override
    public void setTileSize(int tileSize) {
        Properties.getInstance().setTileSize(tileSize);
    }

    @Override
    public IntegerProperty compareSizeProperty() {
        return Properties.getInstance().compareSizeProperty();
    }

    @Override
    public int getCompareSize() {
        return Properties.getInstance().getCompareSize();
    }

    @Override
    public void setCompareSize(int compareSize) {
        Properties.getInstance().setCompareSize(compareSize);
    }

    @Override
    public IntegerProperty opacityProperty() {
        return Properties.getInstance().opacityProperty();
    }

    @Override
    public int getOpacity() {
        return Properties.getInstance().getOpacity();
    }

    @Override
    public void setOpacity(int opacity) {
        Properties.getInstance().setOpacity(opacity);
    }

    @Override
    public IntegerProperty preColorAlignmentProperty() {
        return Properties.getInstance().preColorAlignmentProperty();
    }

    @Override
    public int getPreColorAlignment() {
        return Properties.getInstance().getPreColorAlignment();
    }

    @Override
    public void setPreColorAlignment(int preColorAlignment) {
        Properties.getInstance().setPreColorAlignment(preColorAlignment);
    }

    @Override
    public IntegerProperty postColorAlignmentProperty() {
        return Properties.getInstance().postColorAlignmentProperty();
    }

    @Override
    public int getPostColorAlignment() {
        return Properties.getInstance().getPostColorAlignment();
    }

    @Override
    public void setPostColorAlignment(int postColorAlignment) {
        Properties.getInstance().setPostColorAlignment(postColorAlignment);
    }

    @Override
    public BooleanProperty blurModeProperty() {
        return Properties.getInstance().blurModeProperty();
    }

    @Override
    public boolean isBlurMode() {
        return Properties.getInstance().isBlurMode();
    }

    @Override
    public void setBlurMode(boolean blurMode) {
        Properties.getInstance().setBlurMode(blurMode);
    }

    @Override
    public IntegerProperty maxReusesProperty() {
        return Properties.getInstance().maxReusesProperty();
    }

    @Override
    public int getMaxReuses() {
        return Properties.getInstance().getMaxReuses();
    }

    @Override
    public void setMaxReuses(int maxReuses) {
        Properties.getInstance().setMaxReuses(maxReuses);
    }

    @Override
    public IntegerProperty reuseDistanceProperty() {
        return Properties.getInstance().maxReusesProperty();
    }

    @Override
    public int getReuseDistance() {
        return Properties.getInstance().getReuseDistance();
    }

    @Override
    public void setReuseDistance(int reuseDistance) {
        Properties.getInstance().setReuseDistance(reuseDistance);
    }

    @Override
    public BooleanProperty scanSubFolderProperty() {
        return Properties.getInstance().scanSubFolderProperty();
    }

    @Override
    public boolean isScanSubFolder() {
        return Properties.getInstance().isScanSubFolder();
    }

    @Override
    public void setScanSubFolder(boolean scanSubFolder) {
        Properties.getInstance().setScanSubFolder(scanSubFolder);
    }

    @Override
    public ObjectProperty<Path> imageFileProperty() {
        return imageFile;
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
    public ObjectProperty<Path> mosaikTilesPathProperty() {
        return Properties.getInstance().tilesPathProperty();
    }

    @Override
    public Path getMosaikTilesPath() {
        return Properties.getInstance().getTilesPath();
    }

    @Override
    public void setMosaikTilesPath(Path mosaikTilesPath) {
        Properties.getInstance().setTilesPath(mosaikTilesPath);
    }

    @Override
    public IntegerProperty tilesXProperty() {
        return Properties.getInstance().tilesXProperty();
    }

    @Override
    public int getTilesX() {
        return Properties.getInstance().getTilesX();
    }

    @Override
    public void setTilesX(int tilesX) {
        Properties.getInstance().setTilesX(tilesX);
    }

    @Override
    public void deleteTile(int x, int y) {
        mosaikImage[index(x,y)].setMosaikTileIndex(-1);
        calculateCompositeImage();
    }
}
