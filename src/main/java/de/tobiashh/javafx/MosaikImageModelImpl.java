package de.tobiashh.javafx;

import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.List;

public class MosaikImageModelImpl implements MosaikImageModel {
    private static final File PROPERTY_FILE = new File("mosaik.properties");

    public static final String[] FILE_EXTENSION = {"png", "jpg", "jpeg"};

    private static final String TILES_X_PROPERTY_KEY = "tilesX";
    private static final int TILES_X_DEFAULT = 20;

    private static final String TILE_SIZE_PROPERTY_KEY = "tileSize";
    private static final int TILE_SIZE_DEFAULT = 128;
    private static final int TILE_SIZE_MIN = 2;
    private static final int TILE_SIZE_MAX = 512;

    private final ObjectProperty<File> imagesPath = new SimpleObjectProperty<>();

    private final IntegerProperty tileSize = new SimpleIntegerProperty(TILE_SIZE_DEFAULT);

    private final ObjectProperty<File> imageFile = new SimpleObjectProperty<>();

    private final IntegerProperty tilesX = new SimpleIntegerProperty();

    private final ReadOnlyIntegerWrapper tilesY = new ReadOnlyIntegerWrapper();

    private final ReadOnlyIntegerWrapper tileCount = new ReadOnlyIntegerWrapper();

    private final ReadOnlyIntegerWrapper filesCount = new ReadOnlyIntegerWrapper();

    private final ReadOnlyObjectWrapper<BufferedImage> compositeImage = new ReadOnlyObjectWrapper<>();

    private final List<Tile> tiles = new ArrayList<>();

    private final ImageComparator imageComparator = new ImageComparator();

    public MosaikImageModelImpl() {
        initProperties();
        initChangeListener();
    }

    private void initProperties() {
        Properties properties = (PROPERTY_FILE.exists()) ? loadProperties() : createPropertiesFile();
        setTileSize(Integer.parseInt(properties.getProperty(TILE_SIZE_PROPERTY_KEY, String.valueOf(TILE_SIZE_DEFAULT))));
        setTilesX(Integer.parseInt(properties.getProperty(TILES_X_PROPERTY_KEY, String.valueOf(TILES_X_DEFAULT))));
    }

    private void initChangeListener() {
        imagesPath.addListener((observableValue, oldPath, newPath) -> calculateImagesCount(newPath));
        imageFile.addListener((observable, oldImageFile, newImageFile) -> loadImage(newImageFile));
    }


    private void generateTiles(BufferedImage image) {
        System.out.println("MosaikImageModelImpl.generateTiles");
        System.out.println("image = " + image);
        tiles.clear();
        for (int y = 0; y < getTilesY(); y++) {
            for (int x = 0; x < getTilesX(); x++) {
                int tileSize = getTileSize();
                BufferedImage subImage = image.getSubimage(x * tileSize, y * tileSize, tileSize, tileSize);
                tiles.add(new Tile(subImage));
            }
        }
    }

    private void loadImage(File imageFile) {
        System.out.println("MosaikImageModelImpl.loadImage");
        System.out.println("imageFile = " + imageFile);
        try {
            BufferedImage bufferedImage = ImageIO.read(imageFile);

            tilesY.set(getTilesX() * bufferedImage.getHeight() / bufferedImage.getWidth());

            int imageWidth = getTilesX() * getTileSize();
            int imageHeight = getTilesY() * getTileSize();

            generateTiles(ImageTools.calculateScaledImage(bufferedImage, imageWidth, imageHeight, true));
            compositeImage.set(calculateCompositeImage());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private BufferedImage calculateCompositeImage() {
        System.out.println("MosaikImageModelImpl.calculateCompositeImage");
        BufferedImage retval = new BufferedImage(getTileSize() * getTilesX(), getTileSize() * getTilesY(), BufferedImage.TYPE_INT_RGB);

        Graphics2D graphics = (Graphics2D) retval.getGraphics();

        for (int y = 0; y < getTilesY(); y++) {
            for (int x = 0; x < getTilesX(); x++) {
                graphics.drawImage(tiles.get(x + y * getTilesX()).image, x * getTileSize(), y * getTileSize(), null);
            }
        }

        Random r = new Random();
        for(int i = 0; i < 1000; i++) {
            int x = r.nextInt(getTilesX());
            int y = r.nextInt(getTilesY());
            int tile = r.nextInt(getTilesX() * getTilesY());
            graphics.drawImage(tiles.get(tile).image, x * getTileSize(), y * getTileSize(), null);
        }

        return retval;
    }

    private Properties createPropertiesFile() {
        Properties properties = new Properties();

        properties.setProperty(TILE_SIZE_PROPERTY_KEY, String.valueOf(TILE_SIZE_DEFAULT));
        properties.setProperty(TILES_X_PROPERTY_KEY, String.valueOf(TILES_X_DEFAULT));

        saveProperties(properties);

        return properties;
    }

    private void saveProperties(Properties properties) {
        try (BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(PROPERTY_FILE))) {
            properties.store(stream, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Properties loadProperties() {
        Properties properties = new Properties();

        try (BufferedInputStream stream = new BufferedInputStream(new FileInputStream(PROPERTY_FILE))) {
            properties.load(stream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return properties;
    }

    private void setPowerOf2TileSize(int tileSize) {
        if (tileSize <= 0) throw new IllegalArgumentException();
        int nearestPowerOf2 = Integer.highestOneBit(tileSize) + (tileSize & Integer.highestOneBit(tileSize) >> 1) * 2;
        this.tileSize.set(Math.max(Math.min(nearestPowerOf2, TILE_SIZE_MAX), TILE_SIZE_MIN));
    }

    private void calculateImagesCount(File newPath) {
        if (newPath.exists()) {
            File[] files = newPath.listFiles((dir, name) -> Arrays.stream(FILE_EXTENSION).anyMatch(extension -> name.endsWith(".".concat(extension))));
            if (files != null) {
                filesCount.set(files.length);
            }
        }
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
    public ReadOnlyIntegerProperty filesCountProperty() {
        return filesCount.getReadOnlyProperty();
    }

    @Override
    public int getFilesCount() {
        return filesCount.get();
    }

    @Override
    public ReadOnlyIntegerProperty tileCountProperty() {
        return tileCount.getReadOnlyProperty();
    }

    @Override
    public int getTileCount() {
        return tileCount.get();
    }

    @Override
    public ObjectProperty<File> imageFileProperty() {
        return imageFile;
    }

    @Override
    public File getImageFile() {
        return imageFile.get();
    }

    @Override
    public void setImageFile(File file) {
        imageFile.set(file);
    }

    @Override
    public IntegerProperty tileSizeProperty() {
        return tileSize;
    }

    @Override
    public int getTileSize() {
        return tileSize.get();
    }

    @Override
    public void setTileSize(int tileSize) {
        setPowerOf2TileSize(tileSize);
    }

    @Override
    public ObjectProperty<File> imagesPathProperty() {
        return imagesPath;
    }

    @Override
    public File getImagesPath() {
        return imagesPath.get();
    }

    @Override
    public void setImagesPath(File tilesPath) {
        this.imagesPath.set(tilesPath);
    }

    @Override
    public IntegerProperty tilesXProperty() {
        return tilesX;
    }

    @Override
    public int getTilesX() {
        return tilesX.get();
    }

    @Override
    public void setTilesX(int length) {
        tilesX.set(length);
    }

    public ReadOnlyIntegerProperty tilesYProperty() {
        return tilesY.getReadOnlyProperty();
    }

    public int getTilesY() {
        return tilesY.get();
    }
}
