package de.tobiashh.javafx.tiles;

import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

class OriginalTileTest {

    @Test
    void setMosikTileIDs() {
        OriginalTile tile = new OriginalTile(new BufferedImage(1,1,BufferedImage.TYPE_INT_RGB), 16);
        tile.setDstTileIDs(1, 2, 3);
        assertThat(tile.getDstTileIndex(), is(-1));
        assertThat(tile.getDstTileID(), is(-1));
        tile.incrementDstTileIndex();
        assertThat(tile.getDstTileIndex(), is(0));
        assertThat(tile.getDstTileID(), is(1));
        tile.incrementDstTileIndex();
        assertThat(tile.getDstTileIndex(), is(1));
        assertThat(tile.getDstTileID(), is(2));
        tile.incrementDstTileIndex();
        assertThat(tile.getDstTileIndex(), is(2));
        assertThat(tile.getDstTileID(), is(3));
    }

    @Test
    void hasIndexSet() {
        OriginalTile tile = new OriginalTile(new BufferedImage(1,1,BufferedImage.TYPE_INT_RGB), 16);
        tile.setDstTileIDs(1, 2, 3);
        assertThat(tile.isIndexSet(), is(false));
        tile.incrementDstTileIndex();
        assertThat(tile.isIndexSet(), is(true));
        tile.resetIndex();
        assertThat(tile.isIndexSet(), is(false));
    }

    @Test
    void incrementIndex() {
        OriginalTile tile = new OriginalTile(new BufferedImage(1,1,BufferedImage.TYPE_INT_RGB), 16);
        tile.setDstTileIDs(1, 2, 3);
        assertThat(tile.getDstTileIndex(), is(-1));
        assertThat(tile.incrementDstTileIndex(), is(true));
        assertThat(tile.getDstTileIndex(), is(0));
        assertThat(tile.incrementDstTileIndex(), is(true));
        assertThat(tile.getDstTileIndex(), is(1));
        assertThat(tile.incrementDstTileIndex(), is(true));
        assertThat(tile.getDstTileIndex(), is(2));
        assertThat(tile.incrementDstTileIndex(), is(false));
        assertThat(tile.getDstTileIndex(), is(2));
        tile.resetIndex();
        assertThat(tile.getDstTileIndex(), is(-1));
        tile.addBlockedIds(2);
        assertThat(tile.getDstTileIndex(), is(-1));
        assertThat(tile.incrementDstTileIndex(), is(true));
        assertThat(tile.getDstTileIndex(), is(0));
        assertThat(tile.incrementDstTileIndex(), is(true));
        assertThat(tile.getDstTileIndex(), is(2));
        assertThat(tile.incrementDstTileIndex(), is(false));
        assertThat(tile.getDstTileIndex(), is(2));
    }
}