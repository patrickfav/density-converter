package at.favre.tools.dconvert.converters.scaling;

import at.favre.tools.dconvert.arg.EScalingQuality;
import at.favre.tools.dconvert.arg.ImageType;
import at.favre.tools.dconvert.util.ImageUtil;
import com.mortennobel.imagescaling.*;

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
                case QUALITY:
                    ResampleOp resizeOp = new ResampleOp(dWidth, dHeight);
                    resizeOp.setFilter(ResampleFilters.getLanczos3Filter());
                    imageToScale = resizeOp.filter(imageToScale, null);
                    break;
                case HIGH_QUALITY:
                    imageToScale = new MultiStepLanczos3RescaleOp(dWidth, dHeight).filter(imageToScale, null);
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
            ResampleOp resizeOp = new ResampleOp(dWidth, dHeight);
            resizeOp.setFilter(ResampleFilters.getMitchellFilter());
            return resizeOp.filter(imageToScale, null);
        } else {
            return imageToScale;
        }
    }

    public class MultiStepLanczos3RescaleOp extends AdvancedResizeOp {

        public MultiStepLanczos3RescaleOp(int dstWidth, int dstHeight) {
            super(DimensionConstrain.createAbsolutionDimension(dstWidth, dstHeight));
        }

        public BufferedImage doFilter(BufferedImage img, BufferedImage dest, int dstWidth, int dstHeight) {
            int type = (img.getTransparency() == Transparency.OPAQUE) ?
                    BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
            BufferedImage ret = img;
            int w, h;

            // Use multi-step technique: start with original size, then
            // scale down in multiple passes with drawImage()
            // until the target size is reached
            w = img.getWidth();
            h = img.getHeight();

            do {
                if (w > dstWidth) {
                    w /= 2;
                    if (w < dstWidth) {
                        w = dstWidth;
                    }
                } else {
                    w = dstWidth;
                }

                if (h > dstHeight) {
                    h /= 2;
                    if (h < dstHeight) {
                        h = dstHeight;
                    }
                } else {
                    h = dstHeight;
                }

//                BufferedImage tmp;
//                if (dest!=null && dest.getWidth()== w && dest.getHeight()== h && w==dstWidth && h==dstHeight){
//                    tmp = dest;
//                } else {
//                    tmp = new BufferedImage(w,h,type);
//                }
//                Graphics2D g2 = tmp.createGraphics();
//                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, renderingHintInterpolation);
//                g2.drawImage(ret, 0, 0, w, h, null);
//                g2.dispose();

                ResampleOp resizeOp = new ResampleOp(w, h);
                resizeOp.setFilter(ResampleFilters.getLanczos3Filter());
                ret = resizeOp.filter(ret, null);

//                ret = tmp;
            } while (w != dstWidth || h != dstHeight);

            return ret;
        }
    }
}
