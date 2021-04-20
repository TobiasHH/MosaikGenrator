package de.tobiashh.javafx;

import javafx.beans.property.*;

import java.awt.image.BufferedImage;
import java.nio.file.Path;

public interface MosaicImageModel {
    ReadOnlyStringWrapper statusProperty();

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

    int getTilesPerColumn();

    void generateMosaicImage();

    String getDstTileInformation(int x, int y);

 //   BufferedImage getComposedImagePart(double hoffset, double voffset, double viewportWidth, double viewportHeight);

    void saveMosaicImage(Path path);

    void addAreaOfIntrest(int x, int y);

    void removeAreaOfIntrest(int x, int y);
}
