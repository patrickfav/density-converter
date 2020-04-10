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

package at.favre.tools.dconvert.converters.scaling;

import com.mortennobel.imagescaling.*;
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
        NOBEL_BILINEAR, NOBEL_BICUBUC, NOBEL_NEAREST_NEIGHBOR, NOBEL_LANCZOS3,
        /**
         * Algorithms from https://github.com/coobird/thumbnailator
         */
        THUMBNAILATOR_BILINEAR, THUMBNAILATOR_BICUBUC,
        /**
         * Algorithms from https://github.com/thebuzzmedia/imgscalr
         */
        IMGSCALR_SEVENTH_STEP, IMGSCALR_HALF_STEP,
        /**
         * Combination of bilinear with lanczos3, uses bilinear if target is at least half of src
         */
        PROGRESSIVE_BILINEAR_AND_LANCZOS2, PROGRESSIVE_BILINEAR_AND_LANCZOS3
    }

    public final Type type;

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
            case NOBEL_LANCZOS3:
                return new MultiStepLanczos3RescaleOp(dWidth, dHeight).filter(imageToScale, null);
            case PROGRESSIVE_BILINEAR_AND_LANCZOS2:
                return scaleProgressiveLanczos(imageToScale, dWidth, dHeight, 2);
            case PROGRESSIVE_BILINEAR_AND_LANCZOS3:
                return scaleProgressiveLanczos(imageToScale, dWidth, dHeight, 3);
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

    private BufferedImage scaleProgressiveLanczos(BufferedImage imageToScale, int dstWidth, int dstHeight, float radius) {
        if (dstWidth < (imageToScale.getWidth() / 2) && dstHeight < (imageToScale.getHeight() / 2)) {
            return new ThumbnailnatorProgressiveAlgorithm(RenderingHints.VALUE_INTERPOLATION_BILINEAR).scale(imageToScale, dstWidth, dstHeight);
        } else {
            return new ResampleAlgorithm(new ResampleAlgorithm.LanczosFilter(radius)).scale(imageToScale, dstWidth, dstHeight);
        }
    }

    private final class MultiStepLanczos3RescaleOp extends AdvancedResizeOp {
        private MultiStepLanczos3RescaleOp(int dstWidth, int dstHeight) {
            super(DimensionConstrain.createAbsolutionDimension(dstWidth, dstHeight));
        }

        public BufferedImage doFilter(BufferedImage img, BufferedImage dest, int dstWidth, int dstHeight) {
            BufferedImage ret = img;
            int w, h;

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

                ResampleOp resizeOp = new ResampleOp(w, h);
                resizeOp.setFilter(ResampleFilters.getLanczos3Filter());
                ret = resizeOp.filter(ret, null);
            } while (w != dstWidth || h != dstHeight);

            return ret;
        }
    }

    @Override
    public String toString() {
        return "ProgressiveAlgorithm[" + type + ']';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProgressiveAlgorithm that = (ProgressiveAlgorithm) o;

        return type == that.type;

    }

    @Override
    public int hashCode() {
        return type != null ? type.hashCode() : 0;
    }
}
