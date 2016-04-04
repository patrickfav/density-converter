package at.favre.tools.dconvert.converters.scaling;

import com.mortennobel.imagescaling.ResampleFilter;
import com.mortennobel.imagescaling.ResampleOp;

import java.awt.image.BufferedImage;

/**
 * Created by PatrickF on 03.04.2016.
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
}
