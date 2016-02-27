package at.favre.tools.converter.platforms;

import at.favre.tools.converter.arg.Arguments;
import at.favre.tools.converter.arg.ECompression;

import java.awt.image.BufferedImage;
import java.io.File;

/**
 *
 */
public interface IPlatformConverter {
	void convert(File destinationFolder, BufferedImage rawImage, String targetImageFileName, ECompression srcCompression, Arguments arguments, ConverterCallback callback);
}
