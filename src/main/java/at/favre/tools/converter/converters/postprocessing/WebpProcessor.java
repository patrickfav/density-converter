package at.favre.tools.converter.converters.postprocessing;

import at.favre.tools.converter.ConverterUtil;
import at.favre.tools.converter.arg.Arguments;
import at.favre.tools.converter.arg.ECompression;

import java.io.File;

/**
 * Converts pngs/jpegs to lossless/lossy webp
 */
public class WebpProcessor implements PostProcessor {

	@Override
	public String process(File rawFile) {
		ECompression compression = Arguments.getCompressionType(rawFile);
		File out = new File(rawFile.getParentFile(), ConverterUtil.getFileNameWithoutExtension(rawFile) + ".webp");
		String[] args = new String[]{};
		if (compression == ECompression.PNG || compression == ECompression.GIF) {
			args = new String[]{"-lossless", "-af", "-m", "6"};
		} else if (compression == ECompression.JPG) {
			args = new String[]{"-af", "-m", "6", "-q", "90"};
		}
		return ConverterUtil.runWebP(rawFile, args, out);
	}
}
