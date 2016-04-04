package at.favre.tools.dconvert.converters.scaling;

import com.mortennobel.imagescaling.MultiStepRescaleOp;
import org.imgscalr.Scalr;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * A best of of progressive scaling algorithms from different libs
 */
public class ProgressiveAlgorithm implements ScaleAlgorithm {
    public enum Type {
        /**
         * Algorithms from https://github.com/mortennobel/java-image-scaling
         */
        NOBEL_BILINEAR, NOBEL_BICUBUC, NOBEL_NEAREST_NEIGHBOR,
        /**
         * Algorithms from https://github.com/coobird/thumbnailator
         */
        THUMBNAILATOR_BILINEAR, THUMBNAILATOR_BICUBUC,
        /**
         * Algorithms from https://github.com/thebuzzmedia/imgscalr
         */
        IMGSCALR_SEVENTH_STEP, IMGSCALR_HALF_STEP
    }

    public Type type;

    public ProgressiveAlgorithm(Type type) {
        this.type = type;
    }

    @Override
    public BufferedImage scale(BufferedImage imageToScale, int dWidth, int dHeight) {
        switch (type) {
            case NOBEL_BILINEAR:
                return new MultiStepRescaleOp(dWidth, dHeight, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
                        .filter(imageToScale, null);
            case NOBEL_BICUBUC:
                return new MultiStepRescaleOp(dWidth, dHeight, RenderingHints.VALUE_INTERPOLATION_BICUBIC)
                        .filter(imageToScale, null);
            case NOBEL_NEAREST_NEIGHBOR:
                return new MultiStepRescaleOp(dWidth, dHeight, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR)
                        .filter(imageToScale, null);
            case THUMBNAILATOR_BILINEAR:
                return new ThumbnailnatorProgressiveAlgorithm(RenderingHints.VALUE_INTERPOLATION_BILINEAR).scale(imageToScale, dWidth, dHeight);
            case THUMBNAILATOR_BICUBUC:
                return new ThumbnailnatorProgressiveAlgorithm(RenderingHints.VALUE_INTERPOLATION_BICUBIC).scale(imageToScale, dWidth, dHeight);
            case IMGSCALR_SEVENTH_STEP:
                return Scalr.resize(imageToScale, Scalr.Method.ULTRA_QUALITY, Scalr.Mode.FIT_EXACT, dWidth, dHeight, null);
            case IMGSCALR_HALF_STEP:
                return Scalr.resize(imageToScale, Scalr.Method.QUALITY, Scalr.Mode.FIT_EXACT, dWidth, dHeight, null);
            default:
                throw new IllegalArgumentException("unknown algorithm");
        }
    }
}
