package at.favre.tools.converter.platforms;

import at.favre.tools.converter.graphics.CompressionType;
import at.favre.tools.converter.RoundingHandler;

import java.awt.image.BufferedImage;
import java.io.File;

/**
 *
 */
public interface IPlatformConverter {

	void setup(RoundingHandler roundingHandler);

	void convert(File target, BufferedImage rawImage, double baseScale, String targetImageFileName, CompressionType compressionType, float quality) throws Exception;

}
