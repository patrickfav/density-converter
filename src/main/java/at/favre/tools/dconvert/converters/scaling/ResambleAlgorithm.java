package at.favre.tools.dconvert.converters.scaling;

import com.mortennobel.imagescaling.ResampleFilter;
import com.mortennobel.imagescaling.ResampleOp;

import java.awt.image.BufferedImage;

/**
 * Wrapper for Resamble Algos from Nobel's Lib
 */
public class ResambleAlgorithm implements ScaleAlgorithm {
    private ResampleFilter filter;

    public ResambleAlgorithm(ResampleFilter filter) {
        this.filter = filter;
    }

    @Override
    public BufferedImage scale(BufferedImage imageToScale, int dWidth, int dHeight) {
        ResampleOp resizeOp = new ResampleOp(dWidth, dHeight);
        resizeOp.setFilter(filter);
        return resizeOp.filter(imageToScale, null);
    }

    @Override
    public String toString() {
        return "ResambleAlgorithm[" + filter.getClass().getSimpleName() + ']';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ResambleAlgorithm that = (ResambleAlgorithm) o;

        return filter != null ? filter.equals(that.filter) : that.filter == null;

    }

    @Override
    public int hashCode() {
        return filter != null ? filter.hashCode() : 0;
    }
}
