package de.tobiashh.javafx.tiles;

import de.tobiashh.javafx.compareable.SimpleSquareComparableImage;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.lang.ref.SoftReference;

public class OriginalTile extends SimpleSquareComparableImage {
    private final static Logger LOGGER = LoggerFactory.getLogger(OriginalTile.class.getName());

    private final ObjectProperty<BufferedImage> srcImage = new SimpleObjectProperty<>();
    private final ObjectProperty<BufferedImage> dstImage = new SimpleObjectProperty<>();

    private int opacity = 100;
    private int postColorAlignment = 100;

    private SoftReference<BufferedImage> composedImage = new SoftReference<>(null);

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
                returnValue = new TileComposer(opacity, postColorAlignment).compose(srcImage, dstImage);
                composedImage = new SoftReference<>(returnValue);
            }

            return returnValue;
        }
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
