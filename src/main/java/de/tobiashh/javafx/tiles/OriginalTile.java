package de.tobiashh.javafx.tiles;

import de.tobiashh.javafx.compareable.PHashCoparableImage;
import de.tobiashh.javafx.compareable.PerceptualHashingCoparableImage;
import de.tobiashh.javafx.compareable.SimpleSquareCoparableImage;
import de.tobiashh.javafx.tools.ImageTools;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.awt.image.BufferedImage;
import java.lang.ref.WeakReference;
import java.nio.file.Path;

public class OriginalTile extends SimpleSquareCoparableImage {
    private final ObjectProperty<BufferedImage> srcImage = new SimpleObjectProperty<>();
    private final ObjectProperty<BufferedImage> dstImage = new SimpleObjectProperty<>();

    private int opacity = 100;
    private int postColorAlignment = 100;

    // A List of IDs in the order from the best fitting to the worst fitting mosaik tile
    int[] mosaikTileIDs;
    boolean[] blockedIds;

    IntegerProperty mosikTileIndex = new SimpleIntegerProperty( -1);

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

            BufferedImage retval = composedImage.get();

            if(retval == null) {
                retval = dstImage;

                if(postColorAlignment > 0)
                {
                   retval = ImageTools.colorAlignment(retval,srcImage,postColorAlignment);
                }

                if(opacity < 100)
                {
                    retval = ImageTools.opacityAdaption(retval,srcImage, opacity);
                }

                composedImage = new WeakReference<>(retval);
            }

            return retval;
        }
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

        for (int i = 0; i < ids.length; i++) {
            mosaikTileIDs[i] = ids[i];
            blockedIds[i] = false;
        }

        setMosaikTileIndex(-1);
    }

    public int getMosaikTileID()
    {
        int mosaikTileIndex = getMosaikTileIndex();

        if(mosaikTileIndex == -1 || mosaikTileIDs == null || mosaikTileIndex >= mosaikTileIDs.length){
            return -1;
        }
        else {
            return mosaikTileIDs[mosaikTileIndex];
        }
    }

    public IntegerProperty mosaikTileIndexProperty() {
        return mosikTileIndex;
    }

    public int getMosaikTileIndex() {
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
        int nextIndex = getMosaikTileIndex() + 1;
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
        return getMosaikTileIndex() >= 0;
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

    public ObjectProperty<BufferedImage> srcImageProperty() {
        return srcImage;
    }

    public BufferedImage getSrcImage() { return srcImage.get(); }

    public void setSrcImage(BufferedImage image) { srcImage.set(image); }

    public ObjectProperty<BufferedImage> dstImageProperty() {
        return dstImage;
    }

    public BufferedImage getDstImage() {return dstImage.get(); }

    public void setDstImage(BufferedImage image) { dstImage.set(image); }

}
