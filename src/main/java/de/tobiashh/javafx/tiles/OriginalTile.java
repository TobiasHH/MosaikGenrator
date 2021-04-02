package de.tobiashh.javafx.tiles;

import de.tobiashh.javafx.compareable.SimpleSquareCoparableImage;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

import java.awt.image.BufferedImage;

public class OriginalTile extends SimpleSquareCoparableImage {
    BufferedImage srcImage;

    // A List of IDs in the order from the best fitting to the worst fitting mosaik tile
    int[] mosaikTileIDs;
    boolean[] blockedIds;

    IntegerProperty mosikTileIndex = new SimpleIntegerProperty( -1);

    public OriginalTile(BufferedImage image)
    {
        setDataImage(image);
        this.srcImage = image;
    }

    public void addBlockedIds(int ... ids)
    {
        for (int id : ids) {
            for (int i = 0; i < mosaikTileIDs.length; i++) {
                if(id == mosaikTileIDs[i])
                {
                    blockedIds[i] = true;
                }
            }
        }
    }

    public void setMosikTileIDs(int ... ids)
    {
        mosaikTileIDs = new int[ids.length];
        blockedIds = new boolean[ids.length];

        for (int i = 0; i < ids.length - 1; i++) {
            mosaikTileIDs[i] = ids[i];
            blockedIds[i] = false;
        }

        setMosaikTileIndex(-1);
    }

    public int getMosaikTileID()
    {
        int mosikTileIndex = getMosikTileIndex();

        if(mosikTileIndex == -1 || mosaikTileIDs == null || mosikTileIndex >= mosaikTileIDs.length){
            return -1;
        }
        else {
            return mosaikTileIDs[mosikTileIndex];
        }
    }

    public int getMosaikTileID(int id)
    {
        if(id < 0 || id >= mosaikTileIDs.length)
        {
            return -1;
        }
        return mosaikTileIDs[id];
    }

    public BufferedImage getImage() {
        return srcImage;
    }

    public IntegerProperty mosikTileIndexProperty() {
        return mosikTileIndex;
    }

    public int getMosikTileIndex() {
        return mosikTileIndex.get();
    }

    public boolean setMosaikTileIndex(int mosikTileIndex) {
        if(mosikTileIndex >= mosaikTileIDs.length)
        {
            return false;
        }

        this.mosikTileIndex.set(mosikTileIndex);
        return true;
    }

    public boolean incrementMosaikTileIndex() {
        System.out.println("old: " + getMosikTileIndex());

        int nextIndex = getMosikTileIndex() + 1;
        while(nextIndex < mosaikTileIDs.length)
        {
            if(!blockedIds[nextIndex])
            {
                setMosaikTileIndex(nextIndex);
                return true;
            }

            nextIndex++;
        }

        return false;
    }

    public void resetIndex() {
          setMosaikTileIndex(-1);
    }

    public boolean isIndexSet() {
        return getMosikTileIndex() >= 0;
    }
}
