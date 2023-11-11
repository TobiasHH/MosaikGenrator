package de.tobiashh.javafx.properties;

import de.tobiashh.javafx.Mode;
import javafx.beans.property.ObjectProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class PropertiesManager {
    private final static Logger LOGGER = LoggerFactory.getLogger(PropertiesManager.class.getName());
    private static final Path PROPERTY_FILE = Path.of("mosaic.properties");
    private static final Properties properties = new Properties();

    public final Path DEFAULT_TILES_PATH = Path.of("tiles");
    public final Path DEFAULT_CACHE_PATH = Path.of("cache");

    public Property<Integer> tilesPerRow = new Property<>("tilesPerRow", properties, Integer.class, 20);
    public Property<Integer> tilesPerImage = new Property<>("tilesPerImage", properties, Integer.class, 500);
    public Property<Integer> tileSize = new Property<>("tileSize", properties, Integer.class, 128, 2, 256);
    public Property<Integer> compareSize = new Property<>("compareSize", properties, Integer.class, 8, 2, 256);
    public Property<Integer> opacity = new Property<>("opacity", properties, Integer.class, 100);
    public Property<Integer> preColorAlignment = new Property<>("preColorAlignment", properties, Integer.class, 50);
    public Property<Integer> postColorAlignment = new Property<>("postColorAlignment", properties, Integer.class, 1000);
    public Property<Integer> maxReuses = new Property<>("maxReuses", properties, Integer.class, 0);
    public Property<Integer> reuseDistance = new Property<>("reuseDistance", properties, Integer.class, 1);

    public Property<Path> tilesPath = new Property<>("tilesPath", properties, Path.class, DEFAULT_TILES_PATH);
    public Property<Path> cachePath = new Property<>("cachePath", properties, Path.class, DEFAULT_CACHE_PATH);

    public Property<Mode> mode = new Property<>("mode", properties, Mode.class, Mode.LINEAR_NEW);

    public Property<Boolean> scanSubFolder = new Property<>("scanSubFolder", properties, Boolean.class, true);
    public Property<Boolean> drawDebugInfo = new Property<>("drawDebugInfo", properties, Boolean.class, false);
    public Property<Boolean> isTilesPerImage = new Property<>("isTilesPerImage", properties, Boolean.class, true);

    static {
        loadProperties();
    }

    public PropertiesManager() {
        LOGGER.info("PropertiesManager");
        initProperties();
    }

    private void initProperties() {
        LOGGER.info("initProperties");

        createDefaultTilesPathIfNotExist();
        createDefaultCachePathIfNotExist();

        cleanUpCache();
    }

    private void createDefaultTilesPathIfNotExist() {
        LOGGER.info("createDefaultTilesPathIfNotExist");
        createPathIfNotExist(tilesPath.get(), "tiles");
    }

    private void createDefaultCachePathIfNotExist() {
        LOGGER.info("createDefaultCachePathIfNotExist");
        createPathIfNotExist(cachePath.get(), "cache");
    }

    private void createPathIfNotExist(Path actualPath, String path) {
        LOGGER.info("createPathIfNotExist");
        if (actualPath.toString().equals(path) && Files.notExists(Path.of(path))) {
            CreatePath(path);
        }
    }

    private void CreatePath(String path) {
        try {
            Files.createDirectory(Path.of(path));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void cleanUpCache() {
        LOGGER.info("cleanUpCache");
        Path path = cachePath.get();
        Pattern pattern = Pattern.compile(".*_date(\\d\\d\\d\\d\\d\\d\\d\\d).*");

        try (Stream<Path> paths = Files.list(path)) {
            paths.filter(Files::isRegularFile).forEach(file -> {
                Matcher matcher = pattern.matcher(file.toString());
                if (matcher.matches()) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
                    LocalDate fileDate = LocalDate.parse(matcher.group(1), formatter);
                    LocalDate today = LocalDate.now();
                    if (!today.equals(fileDate)) {
                        deleteFile(file);
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void deleteFile(Path file) {
        try {
            Files.delete(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadProperties(){
        LOGGER.debug("loadProperties");
        if (Files.exists(PROPERTY_FILE)) {
            try (BufferedInputStream stream = new BufferedInputStream(new FileInputStream(PROPERTY_FILE.toFile()))) {
                properties.load(stream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public ObjectProperty<Integer> tileSizeProperty() {
        return tileSize.property();
    }

    public ObjectProperty<Path> tilesPathProperty() {
        return tilesPath.property();
    }

    public ObjectProperty<Path> cachePathProperty() {
        return cachePath.property();
    }

    public ObjectProperty<Integer> tilesPerRowProperty() {
        return tilesPerRow.property();
    }

    public ObjectProperty<Integer> tilesPerImageProperty() {
        return tilesPerImage.property();
    }

    public ObjectProperty<Mode> modeProperty() {
        return mode.property();
    }

    public ObjectProperty<Integer> compareSizeProperty() {
        return compareSize.property();
    }

    public ObjectProperty<Integer> opacityProperty() {
        return opacity.property();
    }

    public ObjectProperty<Integer> preColorAlignmentProperty() {
        return preColorAlignment.property();
    }

    public ObjectProperty<Integer> postColorAlignmentProperty() {
        return postColorAlignment.property();
    }

    public ObjectProperty<Integer> maxReusesProperty() {
        return maxReuses.property();
    }

    public ObjectProperty<Integer> reuseDistanceProperty() {
        return reuseDistance.property();
    }

    public ObjectProperty<Boolean> scanSubFolderProperty() {
        return scanSubFolder.property();
    }

    public ObjectProperty<Boolean> drawDebugInfoProperty() {
        return drawDebugInfo.property();
    }

    public ObjectProperty<Boolean> isTilesPerImageProperty() {
        return isTilesPerImage.property();
    }
}
