package de.tobiashh.javafx.compareable;

import de.tobiashh.javafx.ImageTools;
import de.tobiashh.javafx.properties.Properties;

import java.awt.image.BufferedImage;
/**
 * Created by ts on 31.01.2017.
 */
public class PerceptualHashingCoparableImage extends ComparableImage
{

    public static final int COMPARE_SIZE = Properties.getInstance().getCompareSize();

    /**
     * http://www.hackerfactor.com/blog/?/archives/432-Looks-Like-It.html
     *
     * Reduce size. The fastest way to remove high frequencies and detail is to shrink the image. In this case, shrink it to 8x8 so that there are 64 total pixels. Don't bother keeping the aspect ratio, just crush it down to fit an 8x8 square. This way, the hash will match any variation of the image, regardless of scale or aspect ratio.
     * Reduce color. The tiny 8x8 picture is converted to a grayscale. This changes the hash from 64 pixels (64 red, 64 green, and 64 blue) to 64 total colors.
     * Average the colors. Compute the mean value of the 64 colors.
     * Compute the bits. This is the fun part. Each bit is simply set based on whether the color value is above or below the mean.
     * Construct the hash. Set the 64 bits into a 64-bit integer. The order does not matter, just as long as you are consistent. (I set the bits from left to right, top to bottom using big-endian.)
     * = = 8f373714acfcf4d0
     *
     * The resulting hash won't change if the image is scaled or the aspect ratio changes. Increasing or decreasing the brightness or contrast, or even altering the colors won't dramatically change the hash value. And best of all: this is FAST!
     *
     * If you want to compare two images, construct the hash from each image and count the number of bit positions that are different. (This is a Hamming distance.) A distance of zero indicates that it is likely a very similar picture (or a variation of the same picture). A distance of 5 means a few things may be different, but they are probably still close enough to be similar. But a distance of 10 or more? That's probably a very different picture.
     *
     * @param dataImage
     */

    @Override
    public void calculateData(BufferedImage compareImage) {
        System.out.println("PerceptualHashingCoparableImage.calculateData");
        BufferedImage dataImage = compareImage;

        if(compareImage.getWidth() != COMPARE_SIZE || compareImage.getHeight() != COMPARE_SIZE) {
            dataImage = new BufferedImage(COMPARE_SIZE, COMPARE_SIZE, BufferedImage.TYPE_INT_RGB);
            dataImage.getGraphics().drawImage(compareImage, 0, 0, COMPARE_SIZE, COMPARE_SIZE, null);
        }

        red = new int[COMPARE_SIZE * COMPARE_SIZE];
        green = new int[COMPARE_SIZE * COMPARE_SIZE];
        blue = new int[COMPARE_SIZE * COMPARE_SIZE];

        for (int x = 0; x < COMPARE_SIZE; x++)
        {
            for (int y = 0; y < COMPARE_SIZE; y++)
            {
                int rgb = dataImage.getRGB(x, y);

                red[x + y * COMPARE_SIZE] = getBit(ImageTools.red(rgb)* 8 / 255);
                green[x + y * COMPARE_SIZE] = getBit(ImageTools.green(rgb)* 8 / 255);
                blue[x + y * COMPARE_SIZE] = getBit(ImageTools.blue(rgb)* 8 / 255);
            }
        }
    }

    private int getBit(int red)
    {
        switch (red) {
            case 0:  return 0x00000001;
            case 1:  return 0x00000010;
            case 2:  return 0x00000100;
            case 3:  return 0x00001000;
            case 4: return 0x00010000;
            case 5:  return 0x00100000;
            case 6: return 0x01000000;
            default: return 0x10000000;
        }
    }

    public int compare(ComparableImage ci)
    {
        int bitCount = 0;

        for (int index = 0; index < COMPARE_SIZE * COMPARE_SIZE; index++)
        {
            bitCount += Integer.bitCount(red[index] ^ ci.red[index]) +
            Integer.bitCount (green[index] ^ ci.green[index]) +
            Integer.bitCount (blue[index] ^ ci.blue[index]);
        }

        return bitCount;
    }
}
