package de.tobiashh.javafx;

import javafx.beans.property.*;

import java.awt.image.BufferedImage;
import java.nio.file.Path;

public interface MosaikImageModel {
    ReadOnlyObjectProperty<BufferedImage> compositeImageProperty();
    BufferedImage getCompositeImage();

    ReadOnlyIntegerProperty dstTilesCountProperty();
    int getDstTilesCount();

    BooleanProperty linearModeProperty();
    boolean isLinearMode();
    void setLinearMode(boolean linearMode);

    IntegerProperty tileSizeProperty();
    int getTileSize();
    void setTileSize(int tileSize);

    IntegerProperty compareSizeProperty();
    int getCompareSize();
    void setCompareSize(int compareSize);

    IntegerProperty opacityProperty();
    int getOpacity();
    void setOpacity(int opacity);

    IntegerProperty colorAlignmentProperty();
    int getColorAlignment();
    void setColorAlignment(int colorAlignment);

    BooleanProperty blurModeProperty();
    boolean isBlurMode();
    void setBlurMode(boolean blurMode);

    IntegerProperty maxReusesProperty();
    int getMaxReuses();
    void setMaxReuses(int maxReuses);

    IntegerProperty reuseDistanceProperty();
    int getReuseDistance();
    void setReuseDistance(int reuseDistance);

    BooleanProperty scanSubFolderProperty();
    boolean isScanSubFolder();
    void setScanSubFolder(boolean scanSubFolder);

    ObjectProperty<Path> imageFileProperty();
    Path getImageFile();
    void setImageFile(Path file);

    ObjectProperty<Path> tilesPathProperty();
    Path getTilesPath();
    void setTilesPath(Path tilesPath);

    IntegerProperty tilesXProperty();
    int getTilesX();
    void setTilesX(int tileCount);

    void deleteTile(int x, int y);
}
