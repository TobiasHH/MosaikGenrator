package de.tobiashh.javafx.composer;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

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

    public void setDstTileIDs(List<Integer> ids)
    {
        LOGGER.debug("setDstTileIDs {}", ids);
        dstTileIDs = new int[ids.size()];
        blockedIds = new boolean[ids.size()];

        for (int i = 0; i < ids.size(); i++) {
            dstTileIDs[i] = ids.get(i);
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
