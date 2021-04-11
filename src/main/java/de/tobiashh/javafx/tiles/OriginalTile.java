package de.tobiashh.javafx.tiles;

import de.tobiashh.javafx.compareable.SimpleSquareComparableImage;
import de.tobiashh.javafx.tools.ImageTools;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.awt.image.BufferedImage;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.logging.Logger;

public class OriginalTile extends SimpleSquareComparableImage {
    private final static Logger LOGGER = Logger.getLogger(OriginalTile.class.getName());

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
        LOGGER.info("OriginalTile.OriginalTile with compareSize " + compareSize);
        setDataImage(srcImage, compareSize);
        setSrcImage(srcImage);
        initChangeListener();
    }

    private void initChangeListener() {
        LOGGER.info("OriginalTile.initChangeListener");
        srcImageProperty().addListener((observable, oldValue, newValue) -> composedImage.clear());
        dstImageProperty().addListener((observable, oldValue, newValue) -> composedImage.clear());
    }

    public BufferedImage getComposedImage()
    {
        LOGGER.info("OriginalTile.getComposedImage");
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
        LOGGER.info("OriginalTile.addBlockedIds " + Arrays.toString(ids));
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
        LOGGER.info("OriginalTile.setDstTileIDs " + Arrays.toString(ids));
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
        LOGGER.info("OriginalTile.getDstTileID");
        int dstTileIndex = getDstTileIndex();

        if(dstTileIndex == -1 || dstTileIDs == null || dstTileIndex >= dstTileIDs.length){
            return -1;
        }
        else {
            return dstTileIDs[dstTileIndex];
        }
    }

    public boolean incrementDstTileIndex() {
        LOGGER.info("OriginalTile.incrementDstTileIndex");
        int nextIndex = getDstTileIndex() + 1;
        while(nextIndex < dstTileIDs.length)
        {
            if(!blockedIds[nextIndex])
            {
                setDstTileIndex(nextIndex);
                return true;
            }

            nextIndex++;
        }

        return false;
    }

    public void resetIndex() {
        LOGGER.info("OriginalTile.resetIndex");
          setDstTileIndex(-1);
    }

    public boolean isIndexSet() {
        LOGGER.info("OriginalTile.isIndexSet");
        return getDstTileIndex() >= 0;
    }

    public void setOpacity(int opacity)
    {
        LOGGER.info("OriginalTile.setOpacity to " + opacity + "%");
        this.opacity = opacity;
        composedImage.clear();
    }

    public void setPostColorAlignment(int postColorAlignment) {
        LOGGER.info("OriginalTile.postColorAlignment to " + postColorAlignment + "%");
        this.postColorAlignment = postColorAlignment;
        composedImage.clear();
    }

    public int getDstTileIndex() {
        LOGGER.info("OriginalTile.getDstTileIndex");
        return dstTileIndex.get();
    }

    public void setDstTileIndex(int dstTileIndex) {
        LOGGER.info("OriginalTile.setDstTileIndex to " + dstTileIndex);
        this.dstTileIndex.set(dstTileIndex);
    }

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
