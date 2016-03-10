package at.favre.tools.dconvert.converters.postprocessing;

import at.favre.tools.dconvert.arg.Arguments;
import at.favre.tools.dconvert.arg.ImageType;
import at.favre.tools.dconvert.util.ImageUtil;
import at.favre.tools.dconvert.util.MiscUtil;

import java.io.File;

/**
 * Optimzes jpeg with mozjpeg
 * https://github.com/mozilla/mozjpeg
 */
public class MozJpegProcessor implements PostProcessor {
	@Override
	public String process(File rawFile) {
		if (Arguments.getImageType(rawFile) == ImageType.JPG && rawFile.exists() && rawFile.isFile()) {
			File outFile = new File(rawFile.getParentFile(), MiscUtil.getFileNameWithoutExtension(rawFile) + "_optimized.jpg");

			String[] args = new String[]{"jpegtran", "-outfile", "\"" + outFile.getAbsolutePath() + "\"", "-optimise", "-progressive", "-copy", "none", "\"" + rawFile.getAbsolutePath() + "\""};
			String out = ImageUtil.runCmd(args);

			if (outFile.exists() && outFile.isFile()) {
				if (rawFile.delete()) {
					outFile.renameTo(rawFile);
				}
			}
			return out;
		}
		return "";
	}
}
