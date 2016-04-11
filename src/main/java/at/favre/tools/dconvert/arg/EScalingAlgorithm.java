package at.favre.tools.dconvert.arg;

import at.favre.tools.dconvert.converters.scaling.NaiveGraphics2dAlgorithm;
import at.favre.tools.dconvert.converters.scaling.ProgressiveAlgorithm;
import at.favre.tools.dconvert.converters.scaling.ResambleAlgorithm;
import at.favre.tools.dconvert.converters.scaling.ScaleAlgorithm;
import com.mortennobel.imagescaling.ResampleFilters;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Supported scaling algorithms in dconvert
 */
public enum EScalingAlgorithm {

    LANCZOS1(new ResambleAlgorithm(new ResambleAlgorithm.LanczosFilter(1)), "lanczos1", Arrays.asList(Type.DOWNSCALING, Type.UPSCALING), false),
    LANCZOS2(new ResambleAlgorithm(new ResambleAlgorithm.LanczosFilter(2)), "lanczos2", Arrays.asList(Type.DOWNSCALING, Type.UPSCALING), true),
    LANCZOS3(new ResambleAlgorithm(new ResambleAlgorithm.LanczosFilter(3)), "lanczos3", Arrays.asList(Type.DOWNSCALING, Type.UPSCALING), true),
    LANCZOS4(new ResambleAlgorithm(new ResambleAlgorithm.LanczosFilter(4)), "lanczos4", Arrays.asList(Type.DOWNSCALING, Type.UPSCALING), false),
    LANCZOS5(new ResambleAlgorithm(new ResambleAlgorithm.LanczosFilter(5)), "lanczos5", Arrays.asList(Type.DOWNSCALING, Type.UPSCALING), false),
    MITCHELL(new ResambleAlgorithm(ResampleFilters.getMitchellFilter()), "mitchell", Arrays.asList(Type.DOWNSCALING, Type.UPSCALING), true),
    BSPLINE(new ResambleAlgorithm(ResampleFilters.getBSplineFilter()), "bspline", Arrays.asList(Type.DOWNSCALING, Type.UPSCALING), false),
    HERMITE(new ResambleAlgorithm(ResampleFilters.getHermiteFilter()), "hermite", Arrays.asList(Type.DOWNSCALING, Type.UPSCALING), false),
    NEAREST_NEIGHBOR(new NaiveGraphics2dAlgorithm(RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR), "nearestNeighbor", Arrays.asList(Type.DOWNSCALING, Type.UPSCALING), true),
    BILINEAR_PROGRESSIVE(new ProgressiveAlgorithm(ProgressiveAlgorithm.Type.NOBEL_BILINEAR), "bilinearProgressive", Collections.singletonList(Type.DOWNSCALING), true),
    BICUBIC_PROGRESSIVE(new ProgressiveAlgorithm(ProgressiveAlgorithm.Type.NOBEL_BICUBUC), "bicubicProgressive", Collections.singletonList(Type.DOWNSCALING), true),
    NEAREST_NEIGHBOR_PROGRESSIVE(new ProgressiveAlgorithm(ProgressiveAlgorithm.Type.NOBEL_NEAREST_NEIGHBOR), "nearestNeighborProgressive", Collections.singletonList(Type.DOWNSCALING), false),
    BILINEAR_PROGRESSIVE2(new ProgressiveAlgorithm(ProgressiveAlgorithm.Type.THUMBNAILATOR_BILINEAR), "bilinearProgressive2", Collections.singletonList(Type.DOWNSCALING), false),
    BICUBIC_PROGRESSIVE_SMOOTH(new ProgressiveAlgorithm(ProgressiveAlgorithm.Type.IMGSCALR_SEVENTH_STEP), "bicubicProgressiveSmooth", Collections.singletonList(Type.DOWNSCALING), false),
    BILINEAR_LANCZOS2(new ProgressiveAlgorithm(ProgressiveAlgorithm.Type.PROGRESSIVE_BILINEAR_AND_LANCZOS3), "bilinearLanczos2", Collections.singletonList(Type.DOWNSCALING), true),
    BILINEAR_LANCZOS3(new ProgressiveAlgorithm(ProgressiveAlgorithm.Type.PROGRESSIVE_BILINEAR_AND_LANCZOS3), "bilinearLanczos3", Collections.singletonList(Type.DOWNSCALING), false),
    BICUBIC(new NaiveGraphics2dAlgorithm(RenderingHints.VALUE_INTERPOLATION_BICUBIC), "bicubic", Collections.singletonList(Type.UPSCALING), true),
    BILINEAR(new NaiveGraphics2dAlgorithm(RenderingHints.VALUE_INTERPOLATION_BILINEAR), "bilinear", Collections.singletonList(Type.UPSCALING), true);

    public enum Type {UPSCALING, DOWNSCALING}

    private final ScaleAlgorithm algorithm;
    private final String cliName;
    private final List<Type> supportedForType;
    private final boolean enabled;

    EScalingAlgorithm(ScaleAlgorithm algorithm, String cliName, List<Type> supportedForType, boolean enabled) {
        this.algorithm = algorithm;
        this.cliName = cliName;
        this.supportedForType = supportedForType;
        this.enabled = enabled;
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

    public boolean isEnabled() {
        return enabled;
    }

    public static EScalingAlgorithm getByName(String name) {
        for (EScalingAlgorithm eScalingAlgorithm : getAllEnabled()) {
            if (eScalingAlgorithm.getName().equals(name)) {
                return eScalingAlgorithm;
            }
        }
        return null;
    }

    public static Set<EScalingAlgorithm> getForType(Type type) {
        return getAllEnabled().stream().filter(eScalingAlgorithm -> eScalingAlgorithm.getSupportedForType().contains(type)).collect(Collectors.toSet());
    }

    public static String getCliArgString(Type type) {
        StringBuilder sb = new StringBuilder();
        getAllEnabled().stream().filter(eScalingAlgorithm -> eScalingAlgorithm.getSupportedForType().contains(type)).forEach(eScalingAlgorithm -> {
            sb.append(eScalingAlgorithm.getName()).append("|");
        });
        String argList = sb.toString();
        return argList.substring(0, argList.length() - 1);
    }

    public static Set<EScalingAlgorithm> getAllEnabled() {
        Set<EScalingAlgorithm> set = new HashSet<>();
        for (EScalingAlgorithm eScalingAlgorithm : EScalingAlgorithm.values()) {
            if (eScalingAlgorithm.isEnabled()) {
                set.add(eScalingAlgorithm);
            }
        }
        return set;
    }
}
