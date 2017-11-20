/*
 *  Copyright 2016 Patrick Favre-Bulle
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package at.favre.tools.dconvert.arg;

import at.favre.tools.dconvert.converters.scaling.NaiveGraphics2dAlgorithm;
import at.favre.tools.dconvert.converters.scaling.ProgressiveAlgorithm;
import at.favre.tools.dconvert.converters.scaling.ResampleAlgorithm;
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

    LANCZOS1(new ResampleAlgorithm(new ResampleAlgorithm.LanczosFilter(1)), "lanczos1", new Type[]{Type.DOWNSCALING, Type.UPSCALING}, false),
    LANCZOS2(new ResampleAlgorithm(new ResampleAlgorithm.LanczosFilter(2)), "lanczos2", new Type[]{Type.DOWNSCALING, Type.UPSCALING}, true),
    LANCZOS3(new ResampleAlgorithm(new ResampleAlgorithm.LanczosFilter(3)), "lanczos3", new Type[]{Type.DOWNSCALING, Type.UPSCALING}, true),
    LANCZOS4(new ResampleAlgorithm(new ResampleAlgorithm.LanczosFilter(4)), "lanczos4", new Type[]{Type.DOWNSCALING, Type.UPSCALING}, false),
    LANCZOS5(new ResampleAlgorithm(new ResampleAlgorithm.LanczosFilter(5)), "lanczos5", new Type[]{Type.DOWNSCALING, Type.UPSCALING}, false),
    MITCHELL(new ResampleAlgorithm(ResampleFilters.getMitchellFilter()), "mitchell", new Type[]{Type.DOWNSCALING, Type.UPSCALING}, true),
    BSPLINE(new ResampleAlgorithm(ResampleFilters.getBSplineFilter()), "bspline", new Type[]{Type.DOWNSCALING, Type.UPSCALING}, false),
    HERMITE(new ResampleAlgorithm(ResampleFilters.getHermiteFilter()), "hermite", new Type[]{Type.DOWNSCALING, Type.UPSCALING}, false),
    NEAREST_NEIGHBOR(new NaiveGraphics2dAlgorithm(RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR), "nearestNeighbor", new Type[]{Type.DOWNSCALING, Type.UPSCALING}, true),
    BILINEAR_PROGRESSIVE(new ProgressiveAlgorithm(ProgressiveAlgorithm.Type.NOBEL_BILINEAR), "bilinearProgressive", new Type[]{Type.DOWNSCALING}, true),
    BICUBIC_PROGRESSIVE(new ProgressiveAlgorithm(ProgressiveAlgorithm.Type.NOBEL_BICUBUC), "bicubicProgressive", new Type[]{Type.DOWNSCALING}, true),
    NEAREST_NEIGHBOR_PROGRESSIVE(new ProgressiveAlgorithm(ProgressiveAlgorithm.Type.NOBEL_NEAREST_NEIGHBOR), "nearestNeighborProgressive", new Type[]{Type.DOWNSCALING}, false),
    BILINEAR_PROGRESSIVE2(new ProgressiveAlgorithm(ProgressiveAlgorithm.Type.THUMBNAILATOR_BILINEAR), "bilinearProgressive2", new Type[]{Type.DOWNSCALING}, false),
    BICUBIC_PROGRESSIVE_SMOOTH(new ProgressiveAlgorithm(ProgressiveAlgorithm.Type.IMGSCALR_SEVENTH_STEP), "bicubicProgressiveSmooth", new Type[]{Type.DOWNSCALING}, false),
    BILINEAR_LANCZOS2(new ProgressiveAlgorithm(ProgressiveAlgorithm.Type.PROGRESSIVE_BILINEAR_AND_LANCZOS3), "bilinearLanczos2", new Type[]{Type.DOWNSCALING}, true),
    BILINEAR_LANCZOS3(new ProgressiveAlgorithm(ProgressiveAlgorithm.Type.PROGRESSIVE_BILINEAR_AND_LANCZOS3), "bilinearLanczos3", new Type[]{Type.DOWNSCALING}, false),
    BICUBIC(new NaiveGraphics2dAlgorithm(RenderingHints.VALUE_INTERPOLATION_BICUBIC), "bicubic", new Type[]{Type.UPSCALING}, true),
    BILINEAR(new NaiveGraphics2dAlgorithm(RenderingHints.VALUE_INTERPOLATION_BILINEAR), "bilinear", new Type[]{Type.UPSCALING}, true);

    public enum Type {
        UPSCALING, DOWNSCALING
    }

    private final ScaleAlgorithm algorithm;
    private final String cliName;
    private final List<Type> supportedForType;
    private final boolean enabled;

    EScalingAlgorithm(ScaleAlgorithm algorithm, String cliName, Type[] supportedForType, boolean enabled) {
        this.algorithm = algorithm;
        this.cliName = cliName;
        this.supportedForType = Collections.unmodifiableList(Arrays.asList(supportedForType));
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
