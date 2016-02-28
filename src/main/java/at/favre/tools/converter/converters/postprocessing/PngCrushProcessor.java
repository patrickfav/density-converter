package at.favre.tools.converter.converters.postprocessing;

import at.favre.tools.converter.ConverterUtil;

import java.io.File;

/**
 * Created by PatrickF on 28.02.2016.
 */
public class PngCrushProcessor implements PostProcessor {
	public static final String[] DEFAULT_ARGS = new String[]{"-rem", "alla", "-rem", "text", "-rem", "gAMA", "-rem", "cHRM", "-rem", "iCCP", "-rem", "sRGB"};
	public String[] additionalArgs;

	public PngCrushProcessor() {
		this(DEFAULT_ARGS);
	}

	public PngCrushProcessor(String[] additionalArgs) {
		this.additionalArgs = additionalArgs;
	}

	@Override
	public String process(File rawFile) {
		return ConverterUtil.runPngCrush(rawFile, additionalArgs);
	}
}
