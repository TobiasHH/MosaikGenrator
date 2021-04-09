package de.tobiashh.javafx.tiles;

import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

class OriginalTileTest {

    @Test
    void setMosikTileIDs() {
        OriginalTile tile = new OriginalTile(new BufferedImage(1,1,BufferedImage.TYPE_INT_RGB));
        tile.setMosaicTileIDs(1, 2, 3);
        assertThat(tile.getMosaicTileIndex(), is(-1));
        assertThat(tile.getMosaicTileID(), is(-1));
        tile.incrementMosaicTileIndex();
        assertThat(tile.getMosaicTileIndex(), is(0));
        assertThat(tile.getMosaicTileID(), is(1));
        tile.incrementMosaicTileIndex();
        assertThat(tile.getMosaicTileIndex(), is(1));
        assertThat(tile.getMosaicTileID(), is(2));
        tile.incrementMosaicTileIndex();
        assertThat(tile.getMosaicTileIndex(), is(2));
        assertThat(tile.getMosaicTileID(), is(3));
    }

    @Test
    void hasIndexSet() {
        OriginalTile tile = new OriginalTile(new BufferedImage(1,1,BufferedImage.TYPE_INT_RGB));
        tile.setMosaicTileIDs(1, 2, 3);
        assertThat(tile.isIndexSet(), is(false));
        tile.incrementMosaicTileIndex();
        assertThat(tile.isIndexSet(), is(true));
        tile.resetIndex();
        assertThat(tile.isIndexSet(), is(false));
    }

    @Test
    void incrementIndex() {
        OriginalTile tile = new OriginalTile(new BufferedImage(1,1,BufferedImage.TYPE_INT_RGB));
        tile.setMosaicTileIDs(1, 2, 3);
        assertThat(tile.getMosaicTileIndex(), is(-1));
        assertThat(tile.incrementMosaicTileIndex(), is(true));
        assertThat(tile.getMosaicTileIndex(), is(0));
        assertThat(tile.incrementMosaicTileIndex(), is(true));
        assertThat(tile.getMosaicTileIndex(), is(1));
        assertThat(tile.incrementMosaicTileIndex(), is(true));
        assertThat(tile.getMosaicTileIndex(), is(2));
        assertThat(tile.incrementMosaicTileIndex(), is(false));
        assertThat(tile.getMosaicTileIndex(), is(2));
        tile.resetIndex();
        assertThat(tile.getMosaicTileIndex(), is(-1));
        tile.addBlockedIds(2);
        assertThat(tile.getMosaicTileIndex(), is(-1));
        assertThat(tile.incrementMosaicTileIndex(), is(true));
        assertThat(tile.getMosaicTileIndex(), is(0));
        assertThat(tile.incrementMosaicTileIndex(), is(true));
        assertThat(tile.getMosaicTileIndex(), is(2));
        assertThat(tile.incrementMosaicTileIndex(), is(false));
        assertThat(tile.getMosaicTileIndex(), is(2));
    }
}