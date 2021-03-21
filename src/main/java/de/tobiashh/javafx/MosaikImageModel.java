package de.tobiashh.javafx;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.scene.image.Image;

import java.io.File;
/* Properties from old Project
    #Property File
    #Wed Sep 13 11:35:24 CEST 2017
    LINEAR_MODE=false
    TILE_SIZE=256
    COLOR_ALIGNMENT=85
    BLUR_MODE=false
    PREVIEW_IMAGE_WIDTH=329
    TILES_X=20
    REUSE=100
    SCAN_SUB_FOLDERS=true
    COMPARE_SIZE=16
    DEBUG=false
    OPACITY=88
    REUSE_DISTANCE=8
    IMAGE_FOLDER=C\:\\Users\\ts\\Google Drive\\BilderAdrianCaro\\
 */
public interface MosaikImageModel {
    IntegerProperty tileSizeProperty();

    int getTileSize();

    void setTileSize(int tileSize);

    Image getImage();

    ReadOnlyObjectProperty<Image> imageProperty();

    ObjectProperty<File> imageFileProperty();

    File getImageFile();

    void setImageFile(File file);

    File getTilesPath();

    void setTilesPath(File tilesPath);

    ObjectProperty<File> tilesPathProperty();

    int getTileCount();

    void setTileCount(int tileCount);

    IntegerProperty tileCountProperty();

    int getTilesX();

    void setTilesX(int tileCount);

    IntegerProperty tilesX();
}
