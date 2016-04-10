package at.favre.tools.dconvert.arg;

import at.favre.tools.dconvert.converters.scaling.NaiveGraphics2dAlgorithm;
import at.favre.tools.dconvert.converters.scaling.ProgressiveAlgorithm;
import at.favre.tools.dconvert.converters.scaling.ResambleAlgorithm;
import at.favre.tools.dconvert.converters.scaling.ScaleAlgorithm;
import com.mortennobel.imagescaling.ResampleFilters;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Supported scaling algorithms in dconvert
 */
public enum EScalingAlgorithm {
    LANCZOS3(new ResambleAlgorithm(ResampleFilters.getLanczos3Filter()), "lanczos3", Arrays.asList(Type.DOWNSCALING, Type.UPSCALING)),
    //    MITCHELL(new ResambleAlgorithm(ResampleFilters.getMitchellFilter()), "mitchell", Arrays.asList(Type.DOWNSCALING,Type.UPSCALING)),
//    BSPLINE(new ResambleAlgorithm(ResampleFilters.getBSplineFilter()), "bspline", Arrays.asList(Type.DOWNSCALING,Type.UPSCALING)),
    BILINEAR_PROGRESSIVE(new ProgressiveAlgorithm(ProgressiveAlgorithm.Type.NOBEL_BILINEAR), "bilinearProgressive", Collections.singletonList(Type.DOWNSCALING)),
    //    BICUBIC_PROGRESSIVE(new ProgressiveAlgorithm(ProgressiveAlgorithm.Type.NOBEL_BICUBUC), "bicubicProgressive", Collections.singletonList(Type.DOWNSCALING)),
//    NEAREST_NEIGHBOR_PROGRESSIVE(new ProgressiveAlgorithm(ProgressiveAlgorithm.Type.NOBEL_NEAREST_NEIGHBOR), "nearestNeighborProgressive", Collections.singletonList(Type.DOWNSCALING)),
//    BILINEAR_PROGRESSIVE2(new ProgressiveAlgorithm(ProgressiveAlgorithm.Type.THUMBNAILATOR_BILINEAR), "bilinearProgressive2", Collections.singletonList(Type.DOWNSCALING)),
//    BICUBIC_PROGRESSIVE_SMOOTH(new ProgressiveAlgorithm(ProgressiveAlgorithm.Type.IMGSCALR_SEVENTH_STEP), "bicubicProgressiveSmooth", Collections.singletonList(Type.DOWNSCALING)),
    BILINEAR_LANCZOS3(new ProgressiveAlgorithm(ProgressiveAlgorithm.Type.PROGRESSIVE_BILINEAR_AND_LANCZOS3), "bilinearLanczos3", Collections.singletonList(Type.DOWNSCALING)),
    BICUBIC(new NaiveGraphics2dAlgorithm(RenderingHints.VALUE_INTERPOLATION_BICUBIC), "bicubic", Collections.singletonList(Type.UPSCALING)),
    BILINEAR(new NaiveGraphics2dAlgorithm(RenderingHints.VALUE_INTERPOLATION_BILINEAR), "bilinear", Collections.singletonList(Type.UPSCALING)),
    NEAREST_NEIGHBOR(new NaiveGraphics2dAlgorithm(RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR), "nearestNeighbor", Arrays.asList(Type.DOWNSCALING, Type.UPSCALING));

    public enum Type {UPSCALING, DOWNSCALING}

    ;

    private ScaleAlgorithm algorithm;
    private String cliName;
    private List<Type> supportedForType;

    EScalingAlgorithm(ScaleAlgorithm algorithm, String cliName, List<Type> supportedForType) {
        this.algorithm = algorithm;
        this.cliName = cliName;
        this.supportedForType = supportedForType;
    }

    public ScaleAlgorithm getImplementation() {
        return algorithm;
    }

    public String getName() {
        return cliName;
    }

    public List<Type> getSupportedForType() {
        return supportedForType;
    }

    public static EScalingAlgorithm getByName(String name) {
        for (EScalingAlgorithm eScalingAlgorithm : EScalingAlgorithm.values()) {
            if (eScalingAlgorithm.getName().equals(name)) {
                return eScalingAlgorithm;
            }
        }
        return null;
    }

    public static Set<EScalingAlgorithm> getForType(Type type) {
        Set<EScalingAlgorithm> set = new HashSet<>();
        for (EScalingAlgorithm eScalingAlgorithm : EScalingAlgorithm.values()) {
            if (eScalingAlgorithm.getSupportedForType().contains(type)) {
                set.add(eScalingAlgorithm);
            }
        }
        return set;
    }

    public static String getCliArgString(Type type) {
        StringBuilder sb = new StringBuilder();
        for (EScalingAlgorithm eScalingAlgorithm : EScalingAlgorithm.values()) {
            if (eScalingAlgorithm.getSupportedForType().contains(type)) {
                sb.append(eScalingAlgorithm.getName()).append("|");
            }
        }
        String argList = sb.toString();
        return argList.substring(0, argList.length() - 1);
    }
}
