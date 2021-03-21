package de.tobiashh.javafx;

import javafx.beans.property.*;
import javafx.scene.image.Image;

import java.io.*;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class MosaikImageModelImpl implements MosaikImageModel {
    private static final File PROPERTY_FILE = new File("mosaik.properties");

    public static final String[] FILE_EXTENSION = {"png", "jpg", "jpeg"};

    private static final String TILES_X_PROPERTY_KEY = "tilesX";
    private static final int TILES_X_DEFAULT = 20;

    private static final String TILE_SIZE_PROPERTY_KEY = "tileSize";
    private static final int TILE_SIZE_DEFAULT = 32;
    private static final int TILE_SIZE_MIN = 2;
    private static final int TILE_SIZE_MAX = 512;

    private final ObjectProperty<File> tilesPath = new SimpleObjectProperty<>();

    private final IntegerProperty tileSize = new SimpleIntegerProperty(TILE_SIZE_DEFAULT);

    private final ObjectProperty<File> imageFile = new SimpleObjectProperty<>();

    private final IntegerProperty tileCount = new SimpleIntegerProperty();

    private final IntegerProperty tilesX = new SimpleIntegerProperty();

    private final ReadOnlyObjectWrapper<Image> image = new ReadOnlyObjectWrapper<>();

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
        tilesPath.addListener((observableValue, oldPath, newPath) -> calculateTileCount(newPath));
        tilesPath.addListener((observableValue, oldPath, newPath) -> setTiles(newPath));
    }

    private Properties createPropertiesFile() {
        Properties properties = new Properties();

        properties.setProperty(TILE_SIZE_PROPERTY_KEY, String.valueOf(TILE_SIZE_DEFAULT));

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

    private void calculateTileCount(File newPath) {
        if (newPath.exists()) {
            File[] files = newPath.listFiles((dir, name) -> Arrays.stream(FILE_EXTENSION).anyMatch(extension -> name.endsWith(".".concat(extension))));
            if (files != null) {
                setTileCount(files.length);
            }
        }
    }

    private void setTiles(File newPath) {
        if (newPath.exists()) {
            File[] files = newPath.listFiles((dir, name) -> Arrays.stream(FILE_EXTENSION).anyMatch(extension -> name.endsWith(".".concat(extension))));
            if (files != null) {
                tiles.clear();
                for (File file : files) {
                    tiles.add(new Tile(file));
                }
            }
        }
        runCalculation();
    }

    private void runCalculation() {
        for (Tile tile : tiles) {
            System.out.println(tile.file);
        }

    }

    @Override
    public ReadOnlyObjectProperty<Image> imageProperty()
    {
        return image.getReadOnlyProperty();
    }

    @Override
    public Image getImage()
    {
        return image.get();
    }

    @Override
    public ObjectProperty<File> imageFileProperty() { return imageFile; }

    @Override
    public File getImageFile() {
        return imageFile.get();
    }

    @Override
    public void setImageFile(File file)
    {
        try {
            image.set(new Image(String.valueOf(file.toURI().toURL())));
            imageFile.set(file);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public IntegerProperty tileSizeProperty() { return tileSize; }

    @Override
    public int getTileSize() { return tileSize.get(); }

    @Override
    public void setTileSize(int tileSize) { setPowerOf2TileSize(tileSize); }

    @Override
    public File getTilesPath() { return tilesPath.get(); }

    @Override
    public void setTilesPath(File tilesPath) { this.tilesPath.set(tilesPath); }

    @Override
    public ObjectProperty<File> tilesPathProperty() { return tilesPath; }

    @Override
    public int getTileCount() { return tileCount.get(); }

    @Override
    public void setTileCount(int tileCount) { this.tileCount.set(tileCount); }

    @Override
    public IntegerProperty tileCountProperty() { return tileCount; }

    @Override
    public int getTilesX() { return tilesX.get(); }

    @Override
    public void setTilesX(int length) { tilesX.set(length);}

    @Override
    public IntegerProperty tilesX() { return tilesX; }
}
