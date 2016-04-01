package at.favre.tools.dconvert.converters.scaling;

import at.favre.tools.dconvert.arg.EScalingQuality;
import at.favre.tools.dconvert.arg.ImageType;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Interface for implementing a scale algorithms
 */
public interface IBufferedImageScaler {
	BufferedImage scale(BufferedImage imageToScale, int dWidth, int dHeight, ImageType.ECompression compression, EScalingQuality algorithm, Color background, boolean antiAlias);
}
