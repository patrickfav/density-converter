package at.favre.tools.dconvert.converters.scaling;

import java.awt.image.BufferedImage;

/**
 *
 */
public interface ScaleAlgorithm {
    BufferedImage scale(BufferedImage imageToScale, int dWidth, int dHeight);
}
