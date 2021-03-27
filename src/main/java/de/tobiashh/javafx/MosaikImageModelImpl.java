package de.tobiashh.javafx;

import de.tobiashh.javafx.properties.Properties;
import de.tobiashh.javafx.tiles.MosaikTile;
import de.tobiashh.javafx.tiles.Tile;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class MosaikImageModelImpl implements MosaikImageModel {
    public static final String[] FILE_EXTENSION = {"png", "jpg", "jpeg"};

    private final ReadOnlyIntegerWrapper dstTilesCount = new ReadOnlyIntegerWrapper();
    private final ReadOnlyObjectWrapper<BufferedImage> compositeImage = new ReadOnlyObjectWrapper<>();
    private final ReadOnlyIntegerWrapper tilesY = new ReadOnlyIntegerWrapper();
    private final ReadOnlyIntegerWrapper mosaikLoadProgress = new ReadOnlyIntegerWrapper();

    private final ObjectProperty<Path> imageFile = new SimpleObjectProperty<>();

    private final ImageComparator imageComparator = new ImageComparator();

    private final List<Tile> tiles = new ArrayList<>();

    private MosaikTilesLoaderTask task;

    public MosaikImageModelImpl() {
        super();
        initChangeListener();
        loadMosaikTiles(Properties.getInstance().getTilesPath());
    }

    private void initChangeListener() {
        System.out.println("MosaikImageModelPropsImpl.initChangeListener");
         imageComparator.destinationTiles.addListener((ListChangeListener<MosaikTile>) c -> Platform.runLater(() -> dstTilesCount.set(imageComparator.destinationTiles.size())));

        mosaikLoadProgress.addListener((observable, oldValue, newValue) -> dstTilesCount.set(newValue.intValue()));
        Properties.getInstance().tilesPathProperty().addListener((observableValue, oldPath, newPath) -> loadMosaikTiles(newPath));
        imageFile.addListener((observable, oldImageFile, newImageFile) -> loadImage(newImageFile));
    }


    private void loadMosaikTiles(Path newPath) {
        System.out.println("MosaikImageModelPropsImpl.loadMosaikTiles");
        System.out.println("newPath = " + newPath);

            if (task != null) task.cancel(true);
            task = new MosaikTilesLoaderTask(newPath, isScanSubFolder());
            task.progressProperty().addListener((observable, oldValue, newValue) -> mosaikLoadProgress.set((int) (newValue.doubleValue() * 100)));
            task.setOnSucceeded(event -> {
                imageComparator.setMosaikTiles(task.getValue());
                calculateCompositeImage();
            });
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
    }

    private void loadImage(Path imageFile) {
        System.out.println("MosaikImageModelPropsImpl.loadImage");
        System.out.println("imageFile = " + imageFile);
        try {
            BufferedImage bufferedImage = ImageIO.read(imageFile.toFile());

            tilesY.set(Properties.getInstance().getTilesX() * bufferedImage.getHeight() / bufferedImage.getWidth());

            int imageWidth = Properties.getInstance().getTilesX() * Properties.getInstance().getTileSize();
            int imageHeight = getTilesY() * Properties.getInstance().getTileSize();

            generateTiles(ImageTools.calculateScaledImage(bufferedImage, imageWidth, imageHeight, true));
            calculateCompositeImage();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void generateTiles(BufferedImage image) {
        System.out.println("MosaikImageModelPropsImpl.generateTiles");
        System.out.println("image = " + image);
        tiles.clear();
        for (int y = 0; y < getTilesY(); y++) {
            for (int x = 0; x < Properties.getInstance().getTilesX(); x++) {
                int tileSize = Properties.getInstance().getTileSize();
                BufferedImage subImage = image.getSubimage(x * tileSize, y * tileSize, tileSize, tileSize);
                tiles.add(new Tile(subImage));
            }
        }
    }
    private void calculateCompositeImage() {
        System.out.println("MosaikImageModelImpl.calculateCompositeImage");
        BufferedImage retval = new BufferedImage(Properties.getInstance().getTileSize() * Properties.getInstance().getTilesX(), Properties.getInstance().getTileSize() * getTilesY(), BufferedImage.TYPE_INT_RGB);

        Graphics2D graphics = (Graphics2D) retval.getGraphics();

        graphics.setColor(Color.RED);
        graphics.fillRect(0, 0, retval.getWidth(), retval.getHeight());

        for (int y = 0; y < getTilesY(); y++) {
            for (int x = 0; x < Properties.getInstance().getTilesX(); x++) {
                int index = x + y * Properties.getInstance().getTilesX();
                Tile tile = tiles.get(index);
                if (tile != null) {
                    MosaikTile mosaikTile = imageComparator.compare(tile);
                    if(mosaikTile != null) {
                        graphics.drawImage(mosaikTile.getImage(), x * Properties.getInstance().getTileSize(), y * Properties.getInstance().getTileSize(), null);
                    }
                   else{
                       graphics.drawImage(tile.getImage(), x * Properties.getInstance().getTileSize(), y * Properties.getInstance().getTileSize(), null);
                    }
                }
            }
        }

/*        Random r = new Random();
        for(int i = 0; i < 1000; i++) {
            int x = r.nextInt(getTilesX());
            int y = r.nextInt(getTilesY());
            int tile = r.nextInt(getTilesX() * getTilesY());
            if(tiles.get(tile) != null) {
                graphics.drawImage(tiles.get(tile).image, x * getTileSize(), y * getTileSize(), null);
            }
        }*/

       compositeImage.set(retval);
    }

    public ReadOnlyIntegerProperty tilesYProperty() {
        return tilesY.getReadOnlyProperty();
    }

    public int getTilesY() {
        return tilesY.get();
    }

    public  ReadOnlyIntegerProperty mosaikLoadProgressProperty() {
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
    public IntegerProperty colorAlignmentProperty() {
        return Properties.getInstance().colorAlignmentProperty();
    }

    @Override
    public int getColorAlignment() {
        return Properties.getInstance().getColorAlignment();
    }

    @Override
    public void setColorAlignment(int colorAlignment) {
Properties.getInstance().setColorAlignment(colorAlignment);
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
    public ObjectProperty<Path> tilesPathProperty() {
        return Properties.getInstance().tilesPathProperty();
    }

    @Override
    public Path getTilesPath() {
        return Properties.getInstance().getTilesPath();
    }

    @Override
    public void setTilesPath(Path tilesPath) {
Properties.getInstance().setTilesPath(tilesPath);
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
        System.out.println("MosaikImageModelImpl.deleteTile");
        System.out.println("x = " + x + ", y = " + y);
        tiles.set(x + y * Properties.getInstance().getTilesX(), null);
        calculateCompositeImage();
    }
}
