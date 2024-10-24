package de.tobiashh.javafx.model;

import de.tobiashh.javafx.Controller;
import de.tobiashh.javafx.Mode;
import javafx.beans.property.*;

import java.awt.image.BufferedImage;
import java.nio.file.Path;

public interface MosaicImageModel {

    ReadOnlyIntegerProperty dstTilesCountProperty();

    ReadOnlyIntegerProperty tilesMinNeededProperty();

    ReadOnlyIntegerProperty usedCountProperty();

    ReadOnlyBooleanProperty imageCalculatedProperty();

    ReadOnlyIntegerProperty tilesPerColumnProperty();

    int getTilesPerColumn();

    void generateMosaicImage();

    String getDstTileInformation(int x, int y);

    ReadOnlyIntegerProperty dstTilesLoadProgressProperty();

    void saveMosaicImage(Path path);

    void addAreaOfIntrest(int x, int y);

    void removeAreaOfIntrest(int x, int y);

    void resetAreaOfIntrest();

    BufferedImage getTile(int x, int y, boolean originalImage);

    ObjectProperty<Path> tilesPathProperty();

    ObjectProperty<Path> cachePathProperty();

    ObjectProperty<Path> srcImagePathProperty();

    IntegerProperty tileSizeProperty();

    IntegerProperty opacityProperty();

    IntegerProperty preColorAlignmentProperty();

    IntegerProperty postColorAlignmentProperty();

    ObjectProperty<Mode> modeProperty();

    IntegerProperty reuseDistanceProperty();

    IntegerProperty maxReusesProperty();

    IntegerProperty compareSizeProperty();

    BooleanProperty scanSubFolderProperty();

    BooleanProperty drawDebugInfoProperty();

    IntegerProperty tilesPerRowProperty();

    IntegerProperty tilesPerImageProperty();

    BooleanProperty isTilesPerImageProperty();

    /**
     * Ersätzt das Bild an der Position.
     *
     * @param x Spalte des Tiles
     * @param y Zeile des Tiles
     */
    void replaceTile(int x, int y);

    /**
     * Ignoriert das Bild im gesamten Mosaik
     *
     * @param x Spalte des Tiles
     * @param y Zeile des Tiles
     */
    void ignoreTile(int x, int y);

    void setController(Controller controller);
}
