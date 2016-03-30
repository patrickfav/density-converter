package at.favre.tools.dconvert.converters.scaling;

import at.favre.tools.dconvert.arg.ImageType;
import at.favre.tools.dconvert.util.ImageUtil;
import net.coobird.thumbnailator.makers.FixedSizeThumbnailMaker;
import net.coobird.thumbnailator.resizers.DefaultResizerFactory;
import net.coobird.thumbnailator.resizers.Resizer;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Using https://github.com/coobird/thumbnailator to scale
 */
public class ThumbnailatorScaler implements IBufferedImageScaler {
	@Override
	public BufferedImage scale(BufferedImage imageToScale, int dWidth, int dHeight, ImageType.ECompression compression, Color background, boolean antiAlias) {
		BufferedImage scaledImage = null;
		if (imageToScale != null) {

			Resizer resizer = DefaultResizerFactory.getInstance()
					.getResizer(new Dimension(imageToScale.getWidth(), imageToScale.getHeight()), new Dimension(dWidth, dHeight));

			scaledImage = new FixedSizeThumbnailMaker(dWidth, dHeight, false, true)
					.resizer(resizer)
					.make(imageToScale);

			if (antiAlias) {
				scaledImage = ImageUtil.OP_ANTIALIAS.filter(scaledImage, null);
			}

			if (!compression.hasTransparency) {
				BufferedImage convertedImg = new BufferedImage(scaledImage.getWidth(), scaledImage.getHeight(), BufferedImage.TYPE_INT_RGB);
				convertedImg.getGraphics().drawImage(scaledImage, 0, 0, background, null);
				scaledImage = convertedImg;
			}
		}
		return scaledImage;
	}
}
