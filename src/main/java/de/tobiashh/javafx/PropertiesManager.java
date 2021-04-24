package de.tobiashh.javafx;

import de.tobiashh.javafx.model.Mode;
import javafx.beans.property.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class PropertiesManager {
    private final static Logger LOGGER = LoggerFactory.getLogger(PropertiesManager.class.getName());

    public static final String TILES_PER_ROW_PROPERTY_KEY = "tilesPerRow";
    public static final String TILE_SIZE_PROPERTY_KEY = "tileSize";
    public static final String TILES_PATH_PROPERTY_KEY = "tilesPath";
    public static final String MODE_PROPERTY_KEY = "mode";
    public static final String COMPARE_SIZE_PROPERTY_KEY = "compareSize";
    public static final String OPACITY_PROPERTY_KEY = "opacity";
    public static final String PRE_COLOR_ALIGNMENT_PROPERTY_KEY = "preColorAlignment";
    public static final String POST_COLOR_ALIGNMENT_PROPERTY_KEY = "postColorAlignment";
    public static final String MAX_REUSES_PROPERTY_KEY = "maxReuses";
    public static final String REUSE_DISTANCE_PROPERTY_KEY = "reuseDistance";
    public static final String SCAN_SUB_FOLDER_PROPERTY_KEY = "scanSubFolder";

    private static final int TILES_PER_ROW_DEFAULT = 20;
    private static final int TILE_SIZE_DEFAULT = 128;
    private static final String TILES_PATH_DEFAULT = "tiles";
    private static final Mode MODE_DEFAULT = Mode.LINEAR;
    private static final int COMPARE_SIZE_DEFAULT = 8;
    private static final int OPACITY_DEFAULT = 8;
    private static final int COLOR_ALIGNMENT_DEFAULT = 80;
    private static final int MAX_REUSES_DEFAULT = 0;
    private static final int REUSE_DISTANCE_DEFAULT = 10;
    private static final boolean SCAN_SUB_FOLDER_DEFAULT = true;

    private static final int TILE_SIZE_MIN = 2;
    private static final int TILE_SIZE_MAX = 512;

    private static final int COMPARE_SIZE_MIN = 2;
    private static final int COMPARE_SIZE_MAX = 512;

    private final ObjectProperty<Path> tilesPath = new SimpleObjectProperty<>();
    private final IntegerProperty tileSize = new SimpleIntegerProperty();
    private final IntegerProperty tilesPerRow = new SimpleIntegerProperty();
    private final ObjectProperty<Mode> mode = new SimpleObjectProperty<>();
    private final IntegerProperty compareSize = new SimpleIntegerProperty();
    private final IntegerProperty opacity = new SimpleIntegerProperty();
    private final IntegerProperty preColorAlignment = new SimpleIntegerProperty();
    private final IntegerProperty postColorAlignment = new SimpleIntegerProperty();
    private final IntegerProperty maxReuses = new SimpleIntegerProperty();
    private final IntegerProperty reuseDistance = new SimpleIntegerProperty();
    private final BooleanProperty scanSubFolder = new SimpleBooleanProperty();

    private final Properties properties = new Properties();

    private static final Path PROPERTY_FILE = Path.of("mosaic.properties");

    public PropertiesManager() {
        LOGGER.info("PropertiesManager");
        initProperties();
    }

    private void initProperties() {
        LOGGER.info("initProperties");
        tilesPathProperty().addListener((observable, oldValue, newValue) -> changePropertyAndSave(TILES_PATH_PROPERTY_KEY, newValue.toString()));
        tileSizeProperty().addListener((observable, oldValue, newValue) -> changePropertyAndSave(TILE_SIZE_PROPERTY_KEY, String.valueOf(newValue.intValue())));
        tilesPerRowProperty().addListener((observable, oldValue, newValue) -> changePropertyAndSave(TILES_PER_ROW_PROPERTY_KEY, String.valueOf(newValue.intValue())));
        modeProperty().addListener((observable, oldValue, newValue) -> changePropertyAndSave(MODE_PROPERTY_KEY, String.valueOf(newValue)));
        compareSizeProperty().addListener((observable, oldValue, newValue) -> changePropertyAndSave(COMPARE_SIZE_PROPERTY_KEY, String.valueOf(newValue)));
        opacityProperty().addListener((observable, oldValue, newValue) -> changePropertyAndSave(OPACITY_PROPERTY_KEY, String.valueOf(newValue)));
        preColorAlignmentProperty().addListener((observable, oldValue, newValue) -> changePropertyAndSave(PRE_COLOR_ALIGNMENT_PROPERTY_KEY, String.valueOf(newValue)));
        postColorAlignmentProperty().addListener((observable, oldValue, newValue) -> changePropertyAndSave(POST_COLOR_ALIGNMENT_PROPERTY_KEY, String.valueOf(newValue)));
        maxReusesProperty().addListener((observable, oldValue, newValue) -> changePropertyAndSave(MAX_REUSES_PROPERTY_KEY, String.valueOf(newValue)));
        reuseDistanceProperty().addListener((observable, oldValue, newValue) -> changePropertyAndSave(REUSE_DISTANCE_PROPERTY_KEY, String.valueOf(newValue)));
        scanSubFolderProperty().addListener((observable, oldValue, newValue) -> changePropertyAndSave(SCAN_SUB_FOLDER_PROPERTY_KEY, String.valueOf(newValue)));

        loadProperties();

        tileSize.set(Math.min(TILE_SIZE_MAX, Math.max(TILE_SIZE_MIN, Integer.parseInt(properties.getProperty(TILE_SIZE_PROPERTY_KEY, String.valueOf(TILE_SIZE_DEFAULT))))));
        tilesPerRow.set(Integer.parseInt(properties.getProperty(TILES_PER_ROW_PROPERTY_KEY, String.valueOf(TILES_PER_ROW_DEFAULT))));
        tilesPath.set(Path.of(properties.getProperty(TILES_PATH_PROPERTY_KEY, TILES_PATH_DEFAULT)));
        mode.set(Mode.valueOf(properties.getProperty(MODE_PROPERTY_KEY, MODE_DEFAULT.toString())));
        compareSize.set(Math.min(COMPARE_SIZE_MAX, Math.max(COMPARE_SIZE_MIN, Integer.parseInt(properties.getProperty(COMPARE_SIZE_PROPERTY_KEY, String.valueOf(COMPARE_SIZE_DEFAULT))))));
        opacity.set(Integer.parseInt(properties.getProperty(OPACITY_PROPERTY_KEY, String.valueOf(OPACITY_DEFAULT))));
        preColorAlignment.set(Integer.parseInt(properties.getProperty(PRE_COLOR_ALIGNMENT_PROPERTY_KEY, String.valueOf(COLOR_ALIGNMENT_DEFAULT))));
        postColorAlignment.set(Integer.parseInt(properties.getProperty(POST_COLOR_ALIGNMENT_PROPERTY_KEY, String.valueOf(COLOR_ALIGNMENT_DEFAULT))));
        maxReuses.set(Integer.parseInt(properties.getProperty(MAX_REUSES_PROPERTY_KEY, String.valueOf(MAX_REUSES_DEFAULT))));
        reuseDistance.set(Integer.parseInt(properties.getProperty(REUSE_DISTANCE_PROPERTY_KEY, String.valueOf(REUSE_DISTANCE_DEFAULT))));
        scanSubFolder.set(Boolean.parseBoolean(properties.getProperty(SCAN_SUB_FOLDER_PROPERTY_KEY, String.valueOf(SCAN_SUB_FOLDER_DEFAULT))));

        createDefaultTilesPathIfNotExist();
    }

    private void createDefaultTilesPathIfNotExist() {
        LOGGER.info("createDefaultTilesPathIfNotExist");
        if(tilesPath.getValue().toString().equals(TILES_PATH_DEFAULT) && Files.notExists(Path.of(TILES_PATH_DEFAULT)))
        {
            try {
                Files.createDirectory(Path.of(TILES_PATH_DEFAULT));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void changePropertyAndSave(String key, String value) {
        LOGGER.info("changeProperty {} to {}", key, value);
        properties.setProperty(key, value);
        saveProperties();
    }

    private void saveProperties() {
        LOGGER.debug("saveProperties");
         try (BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(PROPERTY_FILE.toFile()))) {
            properties.store(stream, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadProperties() {
        LOGGER.debug("loadProperties");
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

    public ObjectProperty<Path> tilesPathProperty() {
        return tilesPath;
    }

    public IntegerProperty tilesPerRowProperty() {
        return tilesPerRow;
    }

    public ObjectProperty<Mode> modeProperty() { return mode; }

    public IntegerProperty compareSizeProperty() {
        return compareSize;
    }

    public IntegerProperty opacityProperty() {
        return opacity;
    }

    public IntegerProperty preColorAlignmentProperty() {
        return preColorAlignment;
    }

    public IntegerProperty postColorAlignmentProperty() {
        return postColorAlignment;
    }

    public IntegerProperty maxReusesProperty() {
        return maxReuses;
    }

    public IntegerProperty reuseDistanceProperty() {
        return reuseDistance;
    }

    public BooleanProperty scanSubFolderProperty() {
        return scanSubFolder;
    }
}
