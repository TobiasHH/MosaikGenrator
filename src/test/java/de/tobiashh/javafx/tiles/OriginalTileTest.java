package de.tobiashh.javafx.tiles;

import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

class OriginalTileTest {

    @Test
    void setMosikTileIDs() {
        OriginalTile tile = new OriginalTile(new BufferedImage(1,1,BufferedImage.TYPE_INT_RGB));
        tile.setMosikTileIDs(1, 2, 3);
        assertThat(tile.getMosaikTileIndex(), is(-1));
        assertThat(tile.getMosaikTileID(), is(-1));
        tile.incrementMosaikTileIndex();
        assertThat(tile.getMosaikTileIndex(), is(0));
        assertThat(tile.getMosaikTileID(), is(1));
        tile.incrementMosaikTileIndex();
        assertThat(tile.getMosaikTileIndex(), is(1));
        assertThat(tile.getMosaikTileID(), is(2));
        tile.incrementMosaikTileIndex();
        assertThat(tile.getMosaikTileIndex(), is(2));
        assertThat(tile.getMosaikTileID(), is(3));
    }

    @Test
    void hasIndexSet() {
        OriginalTile tile = new OriginalTile(new BufferedImage(1,1,BufferedImage.TYPE_INT_RGB));
        tile.setMosikTileIDs(1, 2, 3);
        assertThat(tile.isIndexSet(), is(false));
        tile.incrementMosaikTileIndex();
        assertThat(tile.isIndexSet(), is(true));
        tile.resetIndex();
        assertThat(tile.isIndexSet(), is(false));
    }

    @Test
    void incrementIndex() {
        OriginalTile tile = new OriginalTile(new BufferedImage(1,1,BufferedImage.TYPE_INT_RGB));
        tile.setMosikTileIDs(1, 2, 3);
        assertThat(tile.getMosaikTileIndex(), is(-1));
        assertThat(tile.incrementMosaikTileIndex(), is(true));
        assertThat(tile.getMosaikTileIndex(), is(0));
        assertThat(tile.incrementMosaikTileIndex(), is(true));
        assertThat(tile.getMosaikTileIndex(), is(1));
        assertThat(tile.incrementMosaikTileIndex(), is(true));
        assertThat(tile.getMosaikTileIndex(), is(2));
        assertThat(tile.incrementMosaikTileIndex(), is(false));
        assertThat(tile.getMosaikTileIndex(), is(2));
        tile.resetIndex();
        assertThat(tile.getMosaikTileIndex(), is(-1));
        tile.addBlockedIds(2);
        assertThat(tile.getMosaikTileIndex(), is(-1));
        assertThat(tile.incrementMosaikTileIndex(), is(true));
        assertThat(tile.getMosaikTileIndex(), is(0));
        assertThat(tile.incrementMosaikTileIndex(), is(true));
        assertThat(tile.getMosaikTileIndex(), is(2));
        assertThat(tile.incrementMosaikTileIndex(), is(false));
        assertThat(tile.getMosaikTileIndex(), is(2));
    }
}