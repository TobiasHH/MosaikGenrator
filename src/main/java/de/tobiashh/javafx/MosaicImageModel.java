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

    int getCompareSize();

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

    ObjectProperty<Path> srcImageFileProperty();
    Path getSrcImageFile();
    void setSrcImageFile(Path file);

    ObjectProperty<Path> dstTilesPathProperty();
    Path getDstTilesPath();
    void setDstTilesPath(Path dstTilesPath);

    IntegerProperty tilesPerRowProperty();
    int getTilesPerRow();
    void setTilesPerRow(int tileCount);

    void deleteTile(int x, int y);
    void calculateMosaicImage();

    String getDstTileInformation(int x, int y);

    void saveImage(Path path);
}
