package at.favre.tools.dconvert.converters.scaling;

import at.favre.tools.dconvert.arg.EScalingAlgorithm;
import at.favre.tools.dconvert.arg.ImageType;
import org.imgscalr.Scalr;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;

/**
 * Using https://github.com/thebuzzmedia/imgscalr lib with ULTRA_QUALITY
 */
public class ImgscalrScaler implements IBufferedImageScaler {
	@Override
	public BufferedImage scale(BufferedImage imageToScale, int dWidth, int dHeight, ImageType.ECompression compression, EScalingAlgorithm algorithm, Color background, boolean antiAlias) {
		BufferedImage scaledImage = null;
		if (imageToScale != null) {

			BufferedImageOp[] bufferedImageOpArray = new BufferedImageOp[]{};

			if (antiAlias) {
				bufferedImageOpArray = new BufferedImageOp[]{Scalr.OP_ANTIALIAS};
			}

			scaledImage = Scalr.resize(imageToScale, Scalr.Method.ULTRA_QUALITY, Scalr.Mode.FIT_EXACT, dWidth, dHeight, bufferedImageOpArray);

			if (!compression.hasTransparency) {
				BufferedImage convertedImg = new BufferedImage(scaledImage.getWidth(), scaledImage.getHeight(), BufferedImage.TYPE_INT_RGB);
				convertedImg.getGraphics().drawImage(scaledImage, 0, 0, background, null);
				scaledImage = convertedImg;
			}
		}
		return scaledImage;
	}
}
