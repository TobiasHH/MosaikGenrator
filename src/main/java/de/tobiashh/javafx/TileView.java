package de.tobiashh.javafx;

import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.ImageView;

import java.awt.image.BufferedImage;


/**
 * Created by ts on 02.02.2017.
 */
public class TileView extends ImageView {
    private static final int DEFAULT_TILE_SIZE = 1;

    private final IntegerProperty tileSizeProperty = new SimpleIntegerProperty(DEFAULT_TILE_SIZE);
    int tilePositionX;
    int tilePositionY;

    public TileView(int tilePositionX, int tilePositionY, int tileSize) {
        super();

        initPropertyListener();

        this.tilePositionX = tilePositionX;
        this.tilePositionY = tilePositionY;
        setTileSize(tileSize);
    }

    public final void setTileSize(int value) {
        tileSizeProperty.set(value);
    }

    public int getTilePositionX() {
        return tilePositionX;
    }

    public int getTilePositionY() {
        return tilePositionY;
    }

    public void setTile(BufferedImage image) {
        if (image != null) {
            setVisible(false);
            setImage(SwingFXUtils.toFXImage(image, null));
            setVisible(true);
        }
    }

    public void initPropertyListener() {
        tileSizeProperty.addListener((observable, oldValue, newValue) -> {
            setX(newValue.intValue() * tilePositionX);
            setY(newValue.intValue() * tilePositionY);
            setFitWidth(newValue.intValue());
            setFitHeight(newValue.intValue());
        });

    }
}
