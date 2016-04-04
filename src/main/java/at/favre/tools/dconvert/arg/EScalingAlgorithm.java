package at.favre.tools.dconvert.arg;

import at.favre.tools.dconvert.converters.scaling.ProgressiveAlgorithm;
import at.favre.tools.dconvert.converters.scaling.ResambleAlgorithm;
import at.favre.tools.dconvert.converters.scaling.ScaleAlgorithm;
import com.mortennobel.imagescaling.ResampleFilters;

/**
 * Created by PatrickF on 03.04.2016.
 */
public enum EScalingAlgorithm {
    LANCZOS3(new ResambleAlgorithm(ResampleFilters.getLanczos3Filter())),
    MITCHELL(new ResambleAlgorithm(ResampleFilters.getMitchellFilter())),
    BSPLINE(new ResambleAlgorithm(ResampleFilters.getBSplineFilter())),
    BILINEAR_PROGRESSIVE(new ProgressiveAlgorithm(ProgressiveAlgorithm.Type.NOBEL_BILINEAR)),
    BICUBIC_PROGRESSIVE(new ProgressiveAlgorithm(ProgressiveAlgorithm.Type.NOBEL_BICUBUC)),
    NEAREST_NEIGHBOR_PROGRESSIVE(new ProgressiveAlgorithm(ProgressiveAlgorithm.Type.NOBEL_NEAREST_NEIGHBOR)),
    BILINEAR_PROGRESSIVE2(new ProgressiveAlgorithm(ProgressiveAlgorithm.Type.THUMBNAILATOR_BILINEAR)),
    BICUBIC_PROGRESSIVE_SEVENTH(new ProgressiveAlgorithm(ProgressiveAlgorithm.Type.IMGSCALR_SEVENTH_STEP));


    private ScaleAlgorithm algorithm;

    EScalingAlgorithm(ScaleAlgorithm algorithm) {
        this.algorithm = algorithm;
    }
}
