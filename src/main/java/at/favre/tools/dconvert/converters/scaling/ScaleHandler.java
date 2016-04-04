package at.favre.tools.dconvert.converters.scaling;

import at.favre.tools.dconvert.arg.EScalingQuality;
import at.favre.tools.dconvert.arg.ImageType;
import at.favre.tools.dconvert.util.ImageUtil;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Created by PatrickF on 03.04.2016.
 */
public class ScaleHandler {


    public BufferedImage scale(BufferedImage imageToScale, int dWidth, int dHeight, ImageType.ECompression compression, EScalingQuality algorithm, Color background, boolean antiAlias) {

        BufferedImage scaledImage = null;

        if (!compression.hasTransparency) {
            BufferedImage convertedImg = new BufferedImage(scaledImage.getWidth(), scaledImage.getHeight(), BufferedImage.TYPE_INT_RGB);
            convertedImg.getGraphics().drawImage(scaledImage, 0, 0, background, null);
            scaledImage = convertedImg;
        }

        if (antiAlias) {
            scaledImage = ImageUtil.OP_ANTIALIAS.filter(scaledImage, null);
        }

        return scaledImage;
    }
}
