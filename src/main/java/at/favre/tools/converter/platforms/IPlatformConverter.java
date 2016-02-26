package at.favre.tools.converter.platforms;

import at.favre.tools.converter.Arguments;

import java.awt.image.BufferedImage;
import java.io.File;

/**
 *
 */
public interface IPlatformConverter {
	void convert(File destinationFolder, BufferedImage rawImage, String targetImageFileName, Arguments.Compression srcCompression, Arguments arguments, ConverterCallback callback);
}
