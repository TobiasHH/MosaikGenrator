package de.tobiashh.javafx.compareable;

import java.awt.image.BufferedImage;

/**
 * Created by ts on 31.01.2017.
 */
public class PHashComparableImage
{
    /**
     * http://www.hackerfactor.com/blog/?/archives/432-Looks-Like-It.html
     *
     * Getting Funky With pHash
     * While the Average Hash is quick and easy, it may be too rigid of a comparison. For example, it can generate false-misses if there is a gamma correction or a color histogram is applied to the image. This is because the colors move along a non-linear scale -- changing where the "average" is located and therefore changing which bits are above/below the average.
     *
     * A more robust algorithm is used by pHash. (I use my own variation of the algorithm, but it's the same concept.) The pHash approach extends the average approach to the extreme, using a discrete cosine transform (DCT) to reduce the frequencies.
     *
     * Reduce size. Like Average Hash, pHash starts with a small image. However, the image is larger than 8x8; 32x32 is a good size. This is really done to simplify the DCT computation and not because it is needed to reduce the high frequencies.
     * Reduce color. The image is reduced to a grayscale just to further simplify the number of computations.
     * Compute the DCT. The DCT separates the image into a collection of frequencies and scalars. While JPEG uses an 8x8 DCT, this algorithm uses a 32x32 DCT.
     * Reduce the DCT. While the DCT is 32x32, just keep the top-left 8x8. Those represent the lowest frequencies in the picture.
     * Compute the average value. Like the Average Hash, compute the mean DCT value (using only the 8x8 DCT low-frequency values and excluding the first term since the DC coefficient can be significantly different from the other values and will throw off the average). Thanks to David Starkweather for the added information about pHash. He wrote: "the dct hash is based on the low 2D DCT coefficients starting at the second from lowest, leaving out the first DC term. This excludes completely flat image information (i.e. solid colors) from being included in the hash description."
     * Further reduce the DCT. This is the magic step. Set the 64 hash bits to 0 or 1 depending on whether each of the 64 DCT values is above or below the average value. The result doesn't tell us the actual low frequencies; it just tells us the very-rough relative scale of the frequencies to the mean. The result will not vary as long as the overall structure of the image remains the same; this can survive gamma and color histogram adjustments without a problem.
     * Construct the hash. Set the 64 bits into a 64-bit integer. The order does not matter, just as long as you are consistent. To see what this fingerprint looks like, simply set the values (this uses +255 and -255 based on whether the bits are 1 or 0) and convert from the 32x32 DCT (with zeros for the high frequencies) back into the 32x32 image:
     * = 8a0303f6df3ec8cd
     * At first glance, this might look like some random blobs... but look closer. There is a dark ring around her head and the dark horizontal line in the background (right side of the picture) appears as a dark spot.
     *
     * As with the Average Hash, pHash values can be compared using the same Hamming distance algorithm. (Just compare each bit position and count the number of differences.)
     *
     * @param dataImage image with data
     */
    public PHashComparableImage(BufferedImage dataImage)
    {
        System.out.println(dataImage);
    }
}
