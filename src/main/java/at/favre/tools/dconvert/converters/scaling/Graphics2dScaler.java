package at.favre.tools.dconvert.converters.scaling;

import at.favre.tools.dconvert.arg.EScalingAlgorithm;
import at.favre.tools.dconvert.arg.ImageType;
import at.favre.tools.dconvert.util.ImageUtil;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Using java natives Graphics2d with best possible renderhints
 */
public class Graphics2dScaler implements IBufferedImageScaler {
	@Override
	public BufferedImage scale(BufferedImage imageToScale, int dWidth, int dHeight, ImageType.ECompression compression, EScalingAlgorithm algorithm, Color background, boolean antiAlias) {
		BufferedImage scaledImage = null;
		if (imageToScale != null) {
			int imageType = imageToScale.getType();
			if (compression == ImageType.ECompression.PNG || compression == ImageType.ECompression.GIF || imageType == 0) {
				imageType = BufferedImage.TYPE_INT_ARGB;
			}

			scaledImage = new BufferedImage(dWidth, dHeight, imageType);
			Graphics2D graphics2D = scaledImage.createGraphics();
			graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			graphics2D.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
			graphics2D.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
			graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);


			if (compression == ImageType.ECompression.JPG) {
				graphics2D.drawImage(imageToScale, 0, 0, dWidth, dHeight, null);
			} else {
				graphics2D.drawImage(imageToScale, 0, 0, dWidth, dHeight, null);
			}

			graphics2D.dispose();

			if (antiAlias) {
				scaledImage = ImageUtil.OP_ANTIALIAS.filter(scaledImage, null);
			}
		}
		return scaledImage;
	}
}
