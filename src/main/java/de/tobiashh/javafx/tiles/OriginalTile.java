package de.tobiashh.javafx.tiles;

import de.tobiashh.javafx.compareable.SimpleSquareComparableImage;
import de.tobiashh.javafx.tools.ImageTools;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.awt.image.BufferedImage;
import java.lang.ref.WeakReference;

public class OriginalTile extends SimpleSquareComparableImage {
    private final ObjectProperty<BufferedImage> srcImage = new SimpleObjectProperty<>();
    private final ObjectProperty<BufferedImage> dstImage = new SimpleObjectProperty<>();

    private int opacity = 100;
    private int postColorAlignment = 100;

    // A List of IDs in the order from the best fitting to the worst fitting mosaic tile
    int[] mosaicTileIDs;
    boolean[] blockedIds;

    IntegerProperty mosaicTileIndex = new SimpleIntegerProperty( -1);

    WeakReference<BufferedImage> composedImage = new WeakReference<>(null);

    public OriginalTile(BufferedImage srcImage)
    {
        setDataImage(srcImage);
        this.srcImage.set(srcImage);
        initChangeListener();
    }

    private void initChangeListener() {
        srcImage.addListener((observable, oldValue, newValue) -> composedImage.clear());
        dstImage.addListener((observable, oldValue, newValue) -> composedImage.clear());
    }

    public BufferedImage getComposedImage()
    {
        BufferedImage srcImage = this.srcImage.get();
        if(srcImage == null)
        {
            return null;
        }
        else
        {
            BufferedImage dstImage = this.dstImage.get();

            if(dstImage == null)
            {
                return srcImage;
            }

            BufferedImage returnValue = composedImage.get();

            if(returnValue == null) {
                returnValue = dstImage;

                if(postColorAlignment > 0)
                {
                   returnValue = ImageTools.colorAlignment(returnValue,srcImage,postColorAlignment);
                }

                if(opacity < 100)
                {
                    returnValue = ImageTools.opacityAdaption(returnValue,srcImage, opacity);
                }

                composedImage = new WeakReference<>(returnValue);
            }

            return returnValue;
        }
    }

    public void addBlockedIds(int ... ids)
    {
        for (int id : ids) {
            for (int i = 0; i < mosaicTileIDs.length; i++) {
                if(id == mosaicTileIDs[i])
                {
                    blockedIds[i] = true;
                }
            }
        }
    }

    public void setMosaicTileIDs(int ... ids)
    {
        mosaicTileIDs = new int[ids.length];
        blockedIds = new boolean[ids.length];

        for (int i = 0; i < ids.length; i++) {
            mosaicTileIDs[i] = ids[i];
            blockedIds[i] = false;
        }

        setMosaicTileIndex(-1);
    }

    public int getMosaicTileID()
    {
        int mosaicTileIndex = getMosaicTileIndex();

        if(mosaicTileIndex == -1 || mosaicTileIDs == null || mosaicTileIndex >= mosaicTileIDs.length){
            return -1;
        }
        else {
            return mosaicTileIDs[mosaicTileIndex];
        }
    }

    public int getMosaicTileIndex() {
        return mosaicTileIndex.get();
    }

    public void setMosaicTileIndex(int mosaicTileIndex) {
            this.mosaicTileIndex.set(mosaicTileIndex);
    }

    public boolean incrementMosaicTileIndex() {
        int nextIndex = getMosaicTileIndex() + 1;
        while(nextIndex < mosaicTileIDs.length)
        {
            if(!blockedIds[nextIndex])
            {
                setMosaicTileIndex(nextIndex);
                return true;
            }

            nextIndex++;
        }

        return false;
    }

    public void resetIndex() {
          setMosaicTileIndex(-1);
    }

    public boolean isIndexSet() {
        return getMosaicTileIndex() >= 0;
    }

    public void setOpacity(int opacity)
    {
        this.opacity = opacity;
        composedImage.clear();
    }

    public void setPostColorAlignment(int postColorAlignment) {
        this.postColorAlignment = postColorAlignment;
        composedImage.clear();
    }

    public BufferedImage getSrcImage() { return srcImage.get(); }

    public void setDstImage(BufferedImage image) { dstImage.set(image); }

}
