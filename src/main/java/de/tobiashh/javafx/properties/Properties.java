package de.tobiashh.javafx.properties;

import javafx.beans.property.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class Properties {
    private static final String TILES_X_PROPERTY_KEY = "tilesX";
    private static final String TILE_SIZE_PROPERTY_KEY = "tileSize";
    private static final String TILES_PATH_PROPERRY_KEY = "tilesPath";
    private static final String LINEAR_MODE_PROPERTY_KEY = "linearMode";
    private static final String COMPARE_SIZE_PROPERTY_KEY = "compareSize";
    private static final String OPACITY_PROPERTY_KEY = "opacity";
    private static final String PRE_COLOR_ALIGNMENT_PROPERTY_KEY = "preColorAlignment";
    private static final String POST_COLOR_ALIGNMENT_PROPERTY_KEY = "postColorAlignment";
    private static final String BLUR_MODE_PROPERTY_KEY = "blurMode";
    private static final String MAX_REUSES_PROPERTY_KEY = "maxReuses";
    private static final String REUSE_DISTANCE_PROPERTY_KEY = "reuseDistance";
    private static final String SCAN_SUB_FOLDER_PROPERTY_KEY = "scanSubFolder";

    private static final int TILES_X_DEFAULT = 20;
    private static final int TILE_SIZE_DEFAULT = 128;
    private static final String TILES_PATH_DEFAULT = "tiles";
    private static final boolean LINEAR_MODE_DEFAULT = false;
    private static final int COMPARE_SIZE_DEFAULT = 8;
    private static final int OPACITY_DEFAULT = 8;
    private static final int COLOR_ALIGNMENT_DEFAULT = 80;
    private static final boolean BLUR_MODE_DEFAULT = false;
    private static final int MAX_REUSES_DEFAULT = 0;
    private static final int REUSE_DISTANCE_DEFAULT = 10;
    private static final boolean SCAN_SUB_FOLDER_DEFAULT = true;

    private static final int TILE_SIZE_MIN = 2;
    private static final int TILE_SIZE_MAX = 512;

    private final ObjectProperty<Path> tilesPath = new SimpleObjectProperty<>();
    private final IntegerProperty tileSize = new SimpleIntegerProperty();
    private final IntegerProperty tilesX = new SimpleIntegerProperty();
    private final BooleanProperty linearMode = new SimpleBooleanProperty();
    private final IntegerProperty compareSize = new SimpleIntegerProperty();
    private final IntegerProperty opacity = new SimpleIntegerProperty();
    private final IntegerProperty preColorAlignment = new SimpleIntegerProperty();
    private final IntegerProperty postColorAlignment = new SimpleIntegerProperty();
    private final BooleanProperty blurMode = new SimpleBooleanProperty();
    private final IntegerProperty maxReuses = new SimpleIntegerProperty();
    private final IntegerProperty reuseDistance = new SimpleIntegerProperty();
    private final BooleanProperty scanSubFolder = new SimpleBooleanProperty();

    private java.util.Properties properties = new java.util.Properties();
    private static final Path PROPERTY_FILE = Path.of("mosaik.properties");

    private static final Properties OBJ = new Properties();

    public Properties() {
        initProperties();
    }

    public static Properties getInstance() {
        return OBJ;
    }

    private void initProperties() {
        tilesPath.addListener((observable, oldValue, newValue) -> changeProperty(TILES_PATH_PROPERRY_KEY, newValue.toString()));
        tileSize.addListener((observable, oldValue, newValue) -> changeProperty(TILE_SIZE_PROPERTY_KEY, String.valueOf(newValue.intValue())));
        tilesX.addListener((observable, oldValue, newValue) -> changeProperty(TILES_X_PROPERTY_KEY, String.valueOf(newValue.intValue())));
        linearMode.addListener((observable, oldValue, newValue) -> changeProperty(LINEAR_MODE_PROPERTY_KEY, String.valueOf(newValue)));
        compareSize.addListener((observable, oldValue, newValue) -> changeProperty(COMPARE_SIZE_PROPERTY_KEY, String.valueOf(newValue)));
        opacity.addListener((observable, oldValue, newValue) -> changeProperty(OPACITY_PROPERTY_KEY, String.valueOf(newValue)));
        preColorAlignment.addListener((observable, oldValue, newValue) -> changeProperty(PRE_COLOR_ALIGNMENT_PROPERTY_KEY, String.valueOf(newValue)));
        postColorAlignment.addListener((observable, oldValue, newValue) -> changeProperty(POST_COLOR_ALIGNMENT_PROPERTY_KEY, String.valueOf(newValue)));
        blurMode.addListener((observable, oldValue, newValue) -> changeProperty(BLUR_MODE_PROPERTY_KEY, String.valueOf(newValue)));
        maxReuses.addListener((observable, oldValue, newValue) -> changeProperty(MAX_REUSES_PROPERTY_KEY, String.valueOf(newValue)));
        reuseDistance.addListener((observable, oldValue, newValue) -> changeProperty(REUSE_DISTANCE_PROPERTY_KEY, String.valueOf(newValue)));
        scanSubFolder.addListener((observable, oldValue, newValue) -> changeProperty(SCAN_SUB_FOLDER_PROPERTY_KEY, String.valueOf(newValue)));

        loadProperties();

        setTileSize(Integer.parseInt(properties.getProperty(TILE_SIZE_PROPERTY_KEY, String.valueOf(TILE_SIZE_DEFAULT))));
        setTilesX(Integer.parseInt(properties.getProperty(TILES_X_PROPERTY_KEY, String.valueOf(TILES_X_DEFAULT))));
        setTilesPath(Path.of(properties.getProperty(TILES_PATH_PROPERRY_KEY, TILES_PATH_DEFAULT)));
        setLinearMode(Boolean.parseBoolean(properties.getProperty(LINEAR_MODE_PROPERTY_KEY, String.valueOf(LINEAR_MODE_DEFAULT))));
        setCompareSize(Integer.parseInt(properties.getProperty(COMPARE_SIZE_PROPERTY_KEY, String.valueOf(COMPARE_SIZE_DEFAULT))));
        setOpacity(Integer.parseInt(properties.getProperty(OPACITY_PROPERTY_KEY, String.valueOf(OPACITY_DEFAULT))));
        setPreColorAlignment(Integer.parseInt(properties.getProperty(PRE_COLOR_ALIGNMENT_PROPERTY_KEY, String.valueOf(COLOR_ALIGNMENT_DEFAULT))));
        setPostColorAlignment(Integer.parseInt(properties.getProperty(POST_COLOR_ALIGNMENT_PROPERTY_KEY, String.valueOf(COLOR_ALIGNMENT_DEFAULT))));
        setBlurMode(Boolean.parseBoolean(properties.getProperty(BLUR_MODE_PROPERTY_KEY, String.valueOf(BLUR_MODE_DEFAULT))));
        setMaxReuses(Integer.parseInt(properties.getProperty(MAX_REUSES_PROPERTY_KEY, String.valueOf(MAX_REUSES_DEFAULT))));
        setReuseDistance(Integer.parseInt(properties.getProperty(REUSE_DISTANCE_PROPERTY_KEY, String.valueOf(REUSE_DISTANCE_DEFAULT))));
        setScanSubFolder(Boolean.parseBoolean(properties.getProperty(SCAN_SUB_FOLDER_PROPERTY_KEY, String.valueOf(SCAN_SUB_FOLDER_DEFAULT))));
    }

    private void changeProperty(String key, String value) {
        properties.setProperty(key, value);
        saveProperties();
    }

    private void saveProperties() {
         try (BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(PROPERTY_FILE.toFile()))) {
            properties.store(stream, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadProperties() {
    if (Files.exists(PROPERTY_FILE)) {
            try (BufferedInputStream stream = new BufferedInputStream(new FileInputStream(PROPERTY_FILE.toFile()))) {
                properties.load(stream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public IntegerProperty tileSizeProperty() {
        return tileSize;
    }

    public int getTileSize() {
        return tileSize.get();
    }

    public void setTileSize(int tileSize) { setPowerOf2TileSize(tileSize); }

    public ObjectProperty<Path> tilesPathProperty() {
        return tilesPath;
    }

    public Path getTilesPath() {
        return tilesPath.get();
    }

    public void setTilesPath(Path tilesPath) {
        this.tilesPath.set(tilesPath);
    }

    public IntegerProperty tilesXProperty() {
        return tilesX;
    }

    public int getTilesX() {
        return tilesX.get();
    }

    public void setTilesX(int length) {
        tilesX.set(length);
    }

    public BooleanProperty linearModeProperty() {
        return linearMode;
    }

    public boolean isLinearMode() {
        return linearMode.get();
    }

    public void setLinearMode(boolean linearMode) {
        this.linearMode.set(linearMode);
    }

    public IntegerProperty compareSizeProperty() {
        return compareSize;
    }

    public int getCompareSize() {
        return compareSize.get();
    }

    public void setCompareSize(int compareSize) {
        this.compareSize.set(compareSize);
    }

    public IntegerProperty opacityProperty() {
        return opacity;
    }

    public int getOpacity() {
        return opacity.get();
    }

    public void setOpacity(int opacity) {
        this.opacity.set(opacity);
    }

    public IntegerProperty preColorAlignmentProperty() {
        return preColorAlignment;
    }

    public int getPreColorAlignment() {
        return preColorAlignment.get();
    }

    public void setPreColorAlignment(int colorAlignment) {
        this.preColorAlignment.set(colorAlignment);
    }

    public IntegerProperty postColorAlignmentProperty() {
        return postColorAlignment;
    }

    public int getPostColorAlignment() {
        return postColorAlignment.get();
    }

    public void setPostColorAlignment(int colorAlignment) {
        this.postColorAlignment.set(colorAlignment);
    }

    public BooleanProperty blurModeProperty() {
        return blurMode;
    }

    public boolean isBlurMode() {
        return blurMode.get();
    }

    public void setBlurMode(boolean blurMode) {
        this.blurMode.set(blurMode);
    }

    public IntegerProperty maxReusesProperty() {
        return maxReuses;
    }

    public int getMaxReuses() {
        return maxReuses.get();
    }

    public void setMaxReuses(int maxReuses) {
        this.maxReuses.set(maxReuses);
    }

    public IntegerProperty reuseDistanceProperty() {
        return reuseDistance;
    }

    public int getReuseDistance() {
        return reuseDistance.get();
    }

    public void setReuseDistance(int reuseDistance) {
        this.reuseDistance.set(reuseDistance);
    }

    public BooleanProperty scanSubFolderProperty() {
        return scanSubFolder;
    }

    public boolean isScanSubFolder() {
        return scanSubFolder.get();
    }

    public void setScanSubFolder(boolean scanSubFolder) {
        this.scanSubFolder.set(scanSubFolder);
    }

    private void setPowerOf2TileSize(int tileSize) {
        if (tileSize <= 0) throw new IllegalArgumentException();
        int nearestPowerOf2 = Integer.highestOneBit(tileSize) + (tileSize & Integer.highestOneBit(tileSize) >> 1) * 2;
        this.tileSize.set(Math.max(Math.min(nearestPowerOf2, TILE_SIZE_MAX), TILE_SIZE_MIN));
    }
}
