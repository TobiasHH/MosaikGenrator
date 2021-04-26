package de.tobiashh.javafx.tiles;

import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

class IndexManagerTest {

    @Test
    void setMosikTileIDs() {
        IndexManager indexManager = new IndexManager();
        indexManager.setDstTileIDs(1, 2, 3);
        assertThat(indexManager.getDstTileIndex(), is(-1));
        assertThat(indexManager.getDstTileID(), is(-1));
        indexManager.incrementDstTileIndex();
        assertThat(indexManager.getDstTileIndex(), is(0));
        assertThat(indexManager.getDstTileID(), is(1));
        indexManager.incrementDstTileIndex();
        assertThat(indexManager.getDstTileIndex(), is(1));
        assertThat(indexManager.getDstTileID(), is(2));
        indexManager.incrementDstTileIndex();
        assertThat(indexManager.getDstTileIndex(), is(2));
        assertThat(indexManager.getDstTileID(), is(3));
    }

    @Test
    void hasIndexSet() {
        IndexManager indexManager = new IndexManager();
        indexManager.setDstTileIDs(1, 2, 3);
        assertThat(indexManager.isIndexSet(), is(false));
        indexManager.incrementDstTileIndex();
        assertThat(indexManager.isIndexSet(), is(true));
        indexManager.resetIndex();
        assertThat(indexManager.isIndexSet(), is(false));
    }

    @Test
    void incrementIndex() {
        IndexManager indexManager = new IndexManager();
        indexManager.setDstTileIDs(1, 2, 3);
        assertThat(indexManager.getDstTileIndex(), is(-1));
        assertThat(indexManager.incrementDstTileIndex(), is(true));
        assertThat(indexManager.getDstTileIndex(), is(0));
        assertThat(indexManager.incrementDstTileIndex(), is(true));
        assertThat(indexManager.getDstTileIndex(), is(1));
        assertThat(indexManager.incrementDstTileIndex(), is(true));
        assertThat(indexManager.getDstTileIndex(), is(2));
        assertThat(indexManager.incrementDstTileIndex(), is(false));
        assertThat(indexManager.getDstTileIndex(), is(2));
        indexManager.resetIndex();
        assertThat(indexManager.getDstTileIndex(), is(-1));
        indexManager.addBlockedIds(2);
        assertThat(indexManager.getDstTileIndex(), is(-1));
        assertThat(indexManager.incrementDstTileIndex(), is(true));
        assertThat(indexManager.getDstTileIndex(), is(0));
        assertThat(indexManager.incrementDstTileIndex(), is(true));
        assertThat(indexManager.getDstTileIndex(), is(2));
        assertThat(indexManager.incrementDstTileIndex(), is(false));
        assertThat(indexManager.getDstTileIndex(), is(2));
    }
}