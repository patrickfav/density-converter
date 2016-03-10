/*
 * Copyright (C) 2016 Patrick Favre-Bulle
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package at.favre.tools.dconvert.converters.postprocessing;

import at.favre.tools.dconvert.arg.Arguments;
import at.favre.tools.dconvert.arg.ImageType;
import at.favre.tools.dconvert.util.ImageUtil;
import at.favre.tools.dconvert.util.MiscUtil;

import java.io.File;

/**
 * Calls pngcrush on a file
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
		return runPngCrush(rawFile, additionalArgs);
	}

	public String runPngCrush(File target, String[] additionalArgs) {
		if (Arguments.getImageType(target) == ImageType.PNG && target.exists() && target.isFile()) {
			String[] cmdArray = MiscUtil.concat(MiscUtil.concat(new String[]{"pngcrush"}, additionalArgs), new String[]{"-ow", "\"" + target.getAbsoluteFile() + "\""});
			return ImageUtil.runCmd(cmdArray);
		}
		return "";
	}
}
