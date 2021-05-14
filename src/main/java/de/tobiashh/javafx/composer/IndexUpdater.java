package de.tobiashh.javafx.composer;

import de.tobiashh.javafx.TilesStraightDistance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.stream.IntStream;

public class IndexUpdater {
    private final static Logger LOGGER = LoggerFactory.getLogger(IndexUpdater.class.getName());

    private final TilesStraightDistance tilesStraightDistance;
    private final int maxReuses;
    private final int reuseDistance;

    public IndexUpdater(TilesStraightDistance tilesStraightDistance, int maxReuses, int reuseDistance) {
        this.tilesStraightDistance = tilesStraightDistance;
        this.maxReuses = maxReuses;
        this.reuseDistance = reuseDistance;
    }

    public boolean setDstTileIndex(int mosaikImageIndex, IndexManager[] indexManagers) {
        LOGGER.debug("setDstTileIndex");
        if (indexManagers[mosaikImageIndex].getDstTileIndex() > -1) return true;

        indexManagers[mosaikImageIndex].incrementDstTileIndex();

        if (indexManagers[mosaikImageIndex].getDstTileIndex() == -1) return true;

        int tileID = indexManagers[mosaikImageIndex].getDstTileID();

        if (Arrays.stream(indexManagers).filter(indexManager -> indexManager.getDstTileID() == tileID).count() > maxReuses) {
            blockID(tileID, mosaikImageIndex, indexManagers);
        }

        int actualIndex = IntStream.range(0, indexManagers.length).filter(i -> indexManagers[i] == indexManagers[mosaikImageIndex]).findFirst().orElse(-1);
        IntStream.range(0, indexManagers.length).forEach(index -> {
            if (tilesStraightDistance.calculate(actualIndex, index) < reuseDistance) {
                indexManagers[index].addBlockedIds(tileID);
            }
        });

        return false;
    }

    private void blockID(int id, int mosaikImageIndex, IndexManager[] indexManagers) {
        LOGGER.debug("blockID");
        IntStream.range(0, indexManagers.length).forEach(index -> {
            if(index != mosaikImageIndex)
            {
                indexManagers[index].addBlockedIds(id);
            }
        });
    }
}
