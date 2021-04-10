package de.tobiashh.javafx;

import javafx.beans.property.*;

import java.awt.image.BufferedImage;
import java.nio.file.Path;

public interface MosaicImageModel {
    ReadOnlyObjectProperty<BufferedImage> compositeImageProperty();
    BufferedImage getCompositeImage();

    BufferedImage getOriginalImage();

    ReadOnlyIntegerProperty dstTilesCountProperty();

    BooleanProperty linearModeProperty();
    boolean isLinearMode();

    int getTileSize();

    IntegerProperty opacityProperty();
    int getOpacity();
    void setOpacity(int opacity);

    IntegerProperty postColorAlignmentProperty();
    int getPostColorAlignment();
    void setPostColorAlignment(int colorAlignment);

    IntegerProperty preColorAlignmentProperty();
    int getPreColorAlignment();
    void setPreColorAlignment(int colorAlignment);

    BooleanProperty blurModeProperty();
    boolean isBlurMode();

    IntegerProperty maxReusesProperty();
    int getMaxReuses();
    void setMaxReuses(int maxReuses);

    IntegerProperty reuseDistanceProperty();
    int getReuseDistance();
    void setReuseDistance(int reuseDistance);

    BooleanProperty scanSubFolderProperty();
    boolean isScanSubFolder();

    Path getImageFile();
    void setImageFile(Path file);

    ObjectProperty<Path> mosaicTilesPathProperty();
    Path getMosaicTilesPath();
    void setMosaicTilesPath(Path mosaicTilesPath);

    IntegerProperty tilesPerRowProperty();
    int getTilesPerRow();
    void setTilesPerRow(int tileCount);

    void deleteTile(int x, int y);
    void calculateMosaicImage();

    String getMosaicTileInformation(int x, int y);

    void saveImage(Path path);
}
