package at.favre.tools.dconvert.converters.scaling;

import at.favre.tools.dconvert.arg.EScalingQuality;
import at.favre.tools.dconvert.arg.ImageType;
import at.favre.tools.dconvert.util.ImageUtil;
import com.mortennobel.imagescaling.MultiStepRescaleOp;
import com.mortennobel.imagescaling.ResampleFilters;
import com.mortennobel.imagescaling.ResampleOp;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Scaler used by DConvert. Uses a mixture of certain libs
 */
public class DConvertScaler extends ABufferedImageScaler {
    @Override
    public BufferedImage scale(BufferedImage imageToScale, int dWidth, int dHeight, ImageType.ECompression compression, EScalingQuality algorithm, Color background, boolean antiAlias) {

        if (dWidth != imageToScale.getWidth() || dHeight != imageToScale.getHeight()) {
            switch (algorithm) {
                default:
                case BALANCE:
                    imageToScale = autoScale(imageToScale, new Dimension(imageToScale.getWidth(), imageToScale.getHeight()), new Dimension(dWidth, dHeight));
                    break;
                case SPEED:
                    imageToScale = new MultiStepRescaleOp(dWidth, dHeight, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR).filter(imageToScale, null);
                    break;
                case HIGH_QUALITY:
                    ResampleOp resizeOp = new ResampleOp(dWidth, dHeight);
                    resizeOp.setFilter(ResampleFilters.getLanczos3Filter());
                    imageToScale = resizeOp.filter(imageToScale, null);
                    break;
            }
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
            return new MultiStepRescaleOp(dWidth, dHeight, RenderingHints.VALUE_INTERPOLATION_BILINEAR).filter(imageToScale, null);
        } else if (dWidth > origWidth && dHeight > origHeight) {
            return new MultiStepRescaleOp(dWidth, dHeight, RenderingHints.VALUE_INTERPOLATION_BICUBIC).filter(imageToScale, null);
        } else {
            return imageToScale;
        }
    }
}
