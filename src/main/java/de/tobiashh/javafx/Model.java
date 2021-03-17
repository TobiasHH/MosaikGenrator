package de.tobiashh.javafx;

import javafx.beans.property.*;
import javafx.scene.image.Image;

import java.io.*;
import java.util.Properties;

public class Model {

    public static final File PROPERTY_FILE = new File("mosaik.properties");

    private static final String SCALE_PROPERTY_KEY = "scale";
    private static final double SCALE_DEFAULT = 1.0;
    public static final double SCALE_MIN = 0.1;
    public static final double SCALE_MAX = 10;

    private static final String TILE_SIZE_PROPERTY_KEY = "tileSize";
    private static final int TILE_SIZE_DEFAULT = 32;
    public static final int TILE_SIZE_MIN = 2;
    public static final int TILE_SIZE_MAX = 512;

    private DoubleProperty scale = new SimpleDoubleProperty(SCALE_DEFAULT);
    private IntegerProperty tileSize = new SimpleIntegerProperty(TILE_SIZE_DEFAULT);

    private ObjectProperty<Image> image = new SimpleObjectProperty<>();

    public Model() {
        initProperties();
    }

    private void initProperties() {
        Properties properties = (PROPERTY_FILE.exists()) ? loadProperties() : initPropertiesFile();

        setScale(Double.parseDouble(properties.getProperty(SCALE_PROPERTY_KEY, String.valueOf(SCALE_DEFAULT))));
        setTileSize(Integer.parseInt(properties.getProperty(TILE_SIZE_PROPERTY_KEY, String.valueOf(TILE_SIZE_DEFAULT))));
    }

    private Properties initPropertiesFile() {
        Properties properties = new Properties();

        properties.setProperty(SCALE_PROPERTY_KEY, String.valueOf(SCALE_DEFAULT));
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

    // getter, setter, property-accessors

    public double getScale() {
        return scale.get();
    }

    public void setScale(double scale) {
        this.scale.set(Math.max(Math.min(scale, SCALE_MAX), SCALE_MIN));
    }

    public DoubleProperty scaleProperty() {
        return scale;
    }

    public Image getImage() {
        return image.get();
    }

    public void setImage(Image image) {
        this.image.set(image);
    }

    public ObjectProperty<Image> imageProperty() {
        return image;
    }

    public int getTileSize() {
        return tileSize.get();
    }

    public void setTileSize(int tileSize) {
        if (tileSize <= 0) throw new IllegalArgumentException();
        int nearestPowerOf2 = Integer.highestOneBit(tileSize) + (tileSize & Integer.highestOneBit(tileSize) >> 1) * 2;
        this.tileSize.set(Math.max(Math.min(nearestPowerOf2, TILE_SIZE_MAX), TILE_SIZE_MIN));
    }


    private int nearestPow2(int number) {
        return (int)Math.round(Math.log(number) / Math.log(2));
    }

    public IntegerProperty tileSizeProperty() {
        return tileSize;
    }
}
