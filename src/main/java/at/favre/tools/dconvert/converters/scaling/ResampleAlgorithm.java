package at.favre.tools.dconvert.converters.scaling;


import at.favre.tools.dconvert.util.ResampleOp;

import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;

/**
 * Created by PatrickF on 12.04.2016.
 */
public class ResampleAlgorithm implements ScaleAlgorithm {
    private int resampleOp;

    public ResampleAlgorithm(int resampleOp) {
        this.resampleOp = resampleOp;
    }

    @Override
    public BufferedImage scale(BufferedImage imageToScale, int dWidth, int dHeight) {
        BufferedImageOp resampler = new ResampleOp(dWidth, dHeight, resampleOp);
        return resampler.filter(imageToScale, null);
    }

    @Override
    public String toString() {
        return "ResampleAlgorithm[" + resampleOp + ']';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ResampleAlgorithm that = (ResampleAlgorithm) o;

        return resampleOp == that.resampleOp;

    }

    @Override
    public int hashCode() {
        return resampleOp;
    }
}
