package at.favre.tools.dconvert.converters.scaling;

import at.favre.tools.dconvert.arg.EScalingAlgorithm;
import at.favre.tools.dconvert.arg.ImageType;
import at.favre.tools.dconvert.util.ImageUtil;
import net.coobird.thumbnailator.makers.FixedSizeThumbnailMaker;
import net.coobird.thumbnailator.resizers.Resizers;
import org.imgscalr.Scalr;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Scaler used by DConvert. Uses a mixture of certain libs
 */
public class DConvertScaler implements IBufferedImageScaler {
    @Override
    public BufferedImage scale(BufferedImage imageToScale, int dWidth, int dHeight, ImageType.ECompression compression, EScalingAlgorithm algorithm, Color background, boolean antiAlias) {
        switch (algorithm) {
            default:
            case AUTO:
                imageToScale = autoScale(imageToScale, new Dimension(imageToScale.getWidth(), imageToScale.getHeight()), new Dimension(dWidth, dHeight));
            case BICUBIC:
                imageToScale = new FixedSizeThumbnailMaker(dWidth, dHeight, false, true)
                        .resizer(new ThumbnailatorScaler.ProgressiveResizer(RenderingHints.VALUE_INTERPOLATION_BICUBIC)).make(imageToScale);
            case BILINEAR:
                imageToScale = new FixedSizeThumbnailMaker(dWidth, dHeight, false, true)
                        .resizer(new ThumbnailatorScaler.ProgressiveResizer(RenderingHints.VALUE_INTERPOLATION_BILINEAR)).make(imageToScale);
            case NEAREST_NEIGHBOR:
                imageToScale = new FixedSizeThumbnailMaker(dWidth, dHeight, false, true)
                        .resizer(new ThumbnailatorScaler.NearestNeighborResizer()).make(imageToScale);
        }

        if (antiAlias) {
            imageToScale = ImageUtil.OP_ANTIALIAS.filter(imageToScale, null);
        }

        if (!compression.hasTransparency) {
            BufferedImage convertedImg = new BufferedImage(imageToScale.getWidth(), imageToScale.getHeight(), BufferedImage.TYPE_INT_RGB);
            convertedImg.getGraphics().drawImage(imageToScale, 0, 0, background, null);
            imageToScale = convertedImg;
        }

        return imageToScale;
    }

    public BufferedImage autoScale(BufferedImage imageToScale, Dimension originalSize, Dimension targetSize) {
        int origWidth = originalSize.width;
        int origHeight = originalSize.height;
        int dWidth = targetSize.width;
        int dHeight = targetSize.height;

        if (dWidth < origWidth && dHeight < origHeight) {
            if (dWidth < (origWidth / 2) && dHeight < (origHeight / 2)) {
                return new FixedSizeThumbnailMaker(dWidth, dHeight, false, true)
                        .resizer(new ThumbnailatorScaler.ProgressiveResizer(RenderingHints.VALUE_INTERPOLATION_BILINEAR)).make(imageToScale);
            } else {
                return Scalr.resize(imageToScale, Scalr.Method.ULTRA_QUALITY, Scalr.Mode.FIT_EXACT, dWidth, dHeight, null);
            }
        } else if (dWidth > origWidth && dHeight > origHeight) {
            return new FixedSizeThumbnailMaker(dWidth, dHeight, false, true)
                    .resizer(Resizers.BICUBIC).make(imageToScale);
        } else {
            return new FixedSizeThumbnailMaker(dWidth, dHeight, false, true)
                    .resizer(Resizers.NULL).make(imageToScale);
        }
    }
}
