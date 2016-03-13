package at.favre.tools.dconvert.converters.postprocessing;

import at.favre.tools.dconvert.arg.ImageType;
import at.favre.tools.dconvert.converters.Result;
import at.favre.tools.dconvert.util.PostProcessorUtil;

import java.io.File;
import java.util.Collections;

/**
 * Optimzes jpeg with mozjpeg
 * https://github.com/mozilla/mozjpeg
 */
public class MozJpegProcessor implements PostProcessor {
	@Override
	public Result process(File rawFile, boolean keepOriginal) {
		try {
			String[] args = new String[]{"jpegtran", "-outfile", "%%outFilePath%%", "-optimise", "-progressive", "-copy", "none", "%%sourceFilePath%%"};
			return PostProcessorUtil.runImageOptimizer(rawFile, ImageType.JPG, args, keepOriginal);
		} catch (Exception e) {
			return new Result("could not execute post processor " + getClass().getSimpleName(), e, Collections.singletonList(rawFile));
		}
	}
}
