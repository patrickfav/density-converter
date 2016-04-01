package at.favre.tools.dconvert.converters.scaling;

import at.favre.tools.dconvert.arg.EScalingQuality;
import at.favre.tools.dconvert.arg.ImageType;
import com.mortennobel.imagescaling.ResampleFilter;
import com.mortennobel.imagescaling.ResampleFilters;
import com.mortennobel.imagescaling.ResampleOp;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Using mortenNobel's lib https://github.com/mortennobel/java-image-scaling
 */
public class MortennobelScaler extends ABufferedImageScaler {
    @Override
    public BufferedImage scale(BufferedImage imageToScale, int dWidth, int dHeight, ImageType.ECompression compression, EScalingQuality algorithm, Color background, boolean antiAlias) {
        ResampleOp resizeOp = new ResampleOp(dWidth, dHeight);
        resizeOp.setFilter(translate(algorithm));
        BufferedImage scaledImage = resizeOp.filter(imageToScale, null);

        if (!compression.hasTransparency) {
            BufferedImage convertedImg = new BufferedImage(scaledImage.getWidth(), scaledImage.getHeight(), BufferedImage.TYPE_INT_RGB);
            convertedImg.getGraphics().drawImage(scaledImage, 0, 0, background, null);
            scaledImage = convertedImg;
        }

        return scaledImage;
    }

    private ResampleFilter translate(EScalingQuality algorithm) {
        switch (algorithm) {
            default:
            case HIGH_QUALITY:
                return ResampleFilters.getLanczos3Filter();
            case BALANCE:
                return ResampleFilters.getBiCubicFilter();
            case SPEED:
                return ResampleFilters.getBoxFilter();
        }
    }
}
