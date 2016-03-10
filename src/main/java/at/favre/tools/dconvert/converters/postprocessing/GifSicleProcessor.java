package at.favre.tools.dconvert.converters.postprocessing;

import at.favre.tools.dconvert.arg.Arguments;
import at.favre.tools.dconvert.arg.ImageType;
import at.favre.tools.dconvert.util.ImageUtil;
import at.favre.tools.dconvert.util.MiscUtil;

import java.io.File;

/**
 * Optimzes animated gifs with gisicle
 * see http://www.lcdf.org/gifsicle/man.html
 */
public class GifSicleProcessor implements PostProcessor {
	@Override
	public String process(File rawFile) {
		if (Arguments.getImageType(rawFile) == ImageType.GIF && rawFile.exists() && rawFile.isFile()) {
			File outFile = new File(rawFile.getParentFile(), MiscUtil.getFileNameWithoutExtension(rawFile) + "_optimized.gif");

			String[] args = new String[]{"gifsicle", "\"" + rawFile.getAbsolutePath() + "\"", "-I", "-O3", "--no-extensions", "-o", "\"" + outFile.getAbsolutePath() + "\""};
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
