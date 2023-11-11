package de.tobiashh.javafx.properties;

import de.tobiashh.javafx.Mode;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;

public class Property<T> {
    private final static Logger LOGGER = LoggerFactory.getLogger(Property.class.getName());
    private static final Path PROPERTY_FILE = Path.of("mosaic.properties");

    private final Properties properties;
    private final String key;
    private final ObjectProperty<T> property;

    public Property(String key, Properties properties, Class<T> clazz, T defaultValue) {
        this(key, properties, clazz, defaultValue, null,null);
    }

    public Property(String key, Properties properties, Class<T> clazz, T defaultValue, T min, T max) {
        this.key = key;
        this.properties = properties;

        T value = switch (defaultValue) {
                case Integer i_ignored -> getValue(clazz, defaultValue, min, max);
                case Path p_ignored -> clazz.cast(Path.of(properties.getProperty(key, defaultValue.toString())));
                case Mode m_ignored -> clazz.cast(Mode.valueOf(properties.getProperty(key, defaultValue.toString())));
                case Boolean b_ignored -> clazz.cast(Boolean.parseBoolean(properties.getProperty(key, String.valueOf(defaultValue))));
                default -> throw new IllegalStateException("Unexpected value: " + defaultValue);
            };

        this.property = new SimpleObjectProperty<>(value);
        LOGGER.info("{} - {}", key, value);

        property.addListener((observable, oldValue, newValue) -> changePropertyAndSave(key, newValue.toString()));
        changePropertyAndSave(key, value.toString());
    }

    private T getValue(Class<T> clazz, T defaultValue, T min, T max) {
        if(min != null && (Integer)defaultValue < (Integer)min) return min;
        if(max != null && (Integer)defaultValue > (Integer)max) return max;
        return clazz.cast(Integer.parseInt(properties.getProperty(key, String.valueOf(defaultValue))));
    }

    public ObjectProperty<T> property() {
        return property;
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
            throw new RuntimeException(e);
        }
    }
}