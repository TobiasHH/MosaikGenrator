package de.tobiashh.javafx.model;

import de.tobiashh.javafx.Mode;
import javafx.beans.property.*;

import java.awt.image.BufferedImage;
import java.nio.file.Path;

public interface MosaicImageModel {
    ReadOnlyStringWrapper statusProperty();

    ReadOnlyIntegerProperty dstTilesCountProperty();

    ReadOnlyBooleanProperty imageCalculatedProperty();

    ReadOnlyIntegerProperty tilesPerColumnProperty();

    int getTilesPerColumn();

    void generateMosaicImage();

    String getDstTileInformation(int x, int y);

    void saveMosaicImage(Path path);

    void addAreaOfIntrest(int x, int y);

    void removeAreaOfIntrest(int x, int y);

    boolean isAreaOfInterest(int x, int y);

    void resetAreaOfIntrest();

    BufferedImage getTile(int x, int y);

    BufferedImage getOriginalTile(int x, int y);

    ObjectProperty<Path> tilesPathProperty();

    ObjectProperty<Path> srcImagePathProperty();

    IntegerProperty tileSizeProperty();

    IntegerProperty opacityProperty();

    IntegerProperty postColorAlignmentProperty();

    ObjectProperty<Mode> modeProperty();

    IntegerProperty reuseDistanceProperty();

    IntegerProperty maxReusesProperty();

    IntegerProperty compareSizeProperty();

    BooleanProperty scanSubFolderProperty();

    IntegerProperty tilesPerRowProperty();
}
