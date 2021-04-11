package de.tobiashh.javafx.tiles;

import de.tobiashh.javafx.compareable.SimpleSquareComparableImage;
import de.tobiashh.javafx.tools.ImageTools;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.lang.ref.WeakReference;
import java.util.Arrays;

public class OriginalTile extends SimpleSquareComparableImage {
    private final static Logger LOGGER = LoggerFactory.getLogger(OriginalTile.class.getName());

    private final ObjectProperty<BufferedImage> srcImage = new SimpleObjectProperty<>();
    private final ObjectProperty<BufferedImage> dstImage = new SimpleObjectProperty<>();
    private final IntegerProperty dstTileIndex = new SimpleIntegerProperty( -1);

    private int opacity = 100;
    private int postColorAlignment = 100;

    // A List of IDs in the order from the best fitting to the worst fitting mosaic tile
    private int[] dstTileIDs;
    private boolean[] blockedIds;

    private WeakReference<BufferedImage> composedImage = new WeakReference<>(null);

    public OriginalTile(BufferedImage srcImage, int compareSize)
    {
        LOGGER.debug("OriginalTile with compareSize {}", compareSize);
        setDataImage(srcImage, compareSize);
        setSrcImage(srcImage);
        initChangeListener();
    }

    private void initChangeListener() {
        LOGGER.debug("initChangeListener");
        srcImageProperty().addListener((observable, oldValue, newValue) -> composedImage.clear());
        dstImageProperty().addListener((observable, oldValue, newValue) -> composedImage.clear());
    }

    public BufferedImage getComposedImage()
    {
        LOGGER.debug("getComposedImage");
        BufferedImage srcImage = getSrcImage();
        if(srcImage == null)
        {
            return null;
        }
        else
        {
            BufferedImage dstImage = getDstImage();

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

    public void setOpacity(int opacity)
    {
        LOGGER.debug("setOpacity to {}%", opacity);
        this.opacity = opacity;
        composedImage.clear();
    }

    public void setPostColorAlignment(int postColorAlignment) {
        LOGGER.debug("setPostColorAlignment to {}%", postColorAlignment);
        this.postColorAlignment = postColorAlignment;
        composedImage.clear();
    }

    public int getDstTileIndex() { return dstTileIndex.get(); }

    public void setDstTileIndex(int dstTileIndex) { this.dstTileIndex.set(dstTileIndex); }

    public ObjectProperty<BufferedImage> srcImageProperty() {
        return srcImage;
    }

    public BufferedImage getSrcImage() { return srcImage.get(); }

    public void setSrcImage(BufferedImage image) { srcImage.set(image); }

    public ObjectProperty<BufferedImage> dstImageProperty() {
        return dstImage;
    }

    public BufferedImage getDstImage() {
        return dstImage.get();
    }

    public void setDstImage(BufferedImage image) { dstImage.set(image); }
}
