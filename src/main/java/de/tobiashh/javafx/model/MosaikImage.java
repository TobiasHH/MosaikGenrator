package de.tobiashh.javafx.model;

import de.tobiashh.javafx.tiles.OriginalTile;

public class MosaikImage {
    OriginalTile[] imageTiles = {};

    public void setPostColorAlignment(int postColorAlignment) {
        for (OriginalTile originalTile : imageTiles) {
            originalTile.setPostColorAlignment(postColorAlignment);
        }
    }

    public int getLength() {
        return imageTiles.length;
    }

    public void setOpacity(int opacity) {
        for (OriginalTile originalTile : imageTiles) {
            originalTile.setOpacity(opacity);
        }
    }

    public OriginalTile getTile(int mosaikImageIndex) {
        return imageTiles[mosaikImageIndex];
    }

    public void unsetDstImages() {
        for (OriginalTile originalTile : imageTiles) {
            originalTile.setDstImage(null);
        }
    }

    public OriginalTile[] getTiles() {
        return imageTiles;
    }

    public void setTiles(OriginalTile[] tiles) {
        imageTiles = tiles;
    }
}
