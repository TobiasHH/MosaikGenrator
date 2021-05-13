package de.tobiashh.javafx.tiles;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class IndexManager {
    private final static Logger LOGGER = LoggerFactory.getLogger(IndexManager.class.getName());

    private final IntegerProperty dstTileIndex = new SimpleIntegerProperty( -1);
    private int[] dstTileIDs;
    private boolean[] blockedIds;

    public void addBlockedIds(int ... ids)
    {
        LOGGER.debug("addBlockedIds {}", Arrays.toString(ids));
        for (int id : ids) {
            for (int i = 0; i < dstTileIDs.length; i++) {
                if(id == dstTileIDs[i])
                {
                    blockedIds[i] = true;
                }
            }
        }
    }
    public void setDstTileIDs(int ... ids)
    {
        LOGGER.debug("setDstTileIDs {}", Arrays.toString(ids));
        dstTileIDs = new int[ids.length];
        blockedIds = new boolean[ids.length];

        for (int i = 0; i < ids.length; i++) {
            dstTileIDs[i] = ids[i];
            blockedIds[i] = false;
        }

        setDstTileIndex(-1);
    }

    public int getDstTileID()
    {
        LOGGER.debug("getDstTileID");
        int dstTileIndex = getDstTileIndex();

        if(dstTileIndex == -1 || dstTileIDs == null || dstTileIndex >= dstTileIDs.length){
            return -1;
        }
        else {
            return dstTileIDs[dstTileIndex];
        }
    }

    public int[] getDstTileIDs() {
        return dstTileIDs;
    }

    public boolean incrementDstTileIndex() {
        LOGGER.debug("incrementDstTileIndex");
        int nextIndex = getDstTileIndex() + 1;
        while(nextIndex < dstTileIDs.length)
        {
            if(!blockedIds[nextIndex])
            {
                LOGGER.trace("set index to {}", nextIndex);
                setDstTileIndex(nextIndex);
                return true;
            }

            nextIndex++;
        }

        return false;
    }


    public void resetIndex() {
        LOGGER.debug("resetIndex");
        setDstTileIndex(-1);
    }

    public boolean isIndexSet() {
        boolean isIndexSet = getDstTileIndex() >= 0;
        LOGGER.debug("isIndexSet {}", isIndexSet);
        return isIndexSet;
    }

    public int getDstTileIndex() { return dstTileIndex.get(); }

    public void setDstTileIndex(int dstTileIndex) { this.dstTileIndex.set(dstTileIndex); }
}
