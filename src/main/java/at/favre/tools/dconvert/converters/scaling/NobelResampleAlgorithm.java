package at.favre.tools.dconvert.converters.scaling;

import at.favre.tools.dconvert.util.ResampleOp2;
import com.mortennobel.imagescaling.ResampleFilter;

import java.awt.image.BufferedImage;

/**
 * Wrapper for Resamble Algos from Nobel's Lib
 */
public class NobelResampleAlgorithm implements ScaleAlgorithm {
    private ResampleFilter filter;

    public NobelResampleAlgorithm(ResampleFilter filter) {
        this.filter = filter;
    }

    @Override
    public BufferedImage scale(BufferedImage imageToScale, int dWidth, int dHeight) {
        ResampleOp2 resizeOp = new ResampleOp2(dWidth, dHeight);
        resizeOp.setFilter(filter);
        return resizeOp.filter(imageToScale, null);
    }

    public static class LanczosFilter implements ResampleFilter {
        private final static float PI_FLOAT = (float) Math.PI;
        private final float radius;

        public LanczosFilter(float radius) {
            this.radius = radius;
        }

        private float sincModified(float value) {
            return ((float) Math.sin(value)) / value;
        }

        public final float apply(float value) {
            if (value == 0) {
                return 1.0f;
            }
            if (value < 0.0f) {
                value = -value;
            }

            if (value < radius) {
                value *= PI_FLOAT;
                return sincModified(value) * sincModified(value / radius);
            } else {
                return 0.0f;
            }
        }

        public float getSamplingRadius() {
            return radius;
        }

        public String getName() {
            return "Lanczos" + (int) radius;
        }
    }

    @Override
    public String toString() {
        return "ResambleAlgorithm[" + filter.getName() + ']';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NobelResampleAlgorithm that = (NobelResampleAlgorithm) o;

        return filter != null ? filter.equals(that.filter) : that.filter == null;

    }

    @Override
    public int hashCode() {
        return filter != null ? filter.hashCode() : 0;
    }
}
