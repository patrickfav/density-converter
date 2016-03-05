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
 * Converts pngs/jpegs to lossless/lossy webp
 */
public class WebpProcessor implements PostProcessor {

	@Override
	public String process(File rawFile) {
		ImageType compression = Arguments.getImageType(rawFile);
		File out = new File(rawFile.getParentFile(), MiscUtil.getFileNameWithoutExtension(rawFile) + ".webp");
		String[] args = new String[]{};
		if (compression == ImageType.PNG || compression == ImageType.GIF) {
			args = new String[]{"-lossless", "-alpha_filter", "best", "-m", "6"};
		} else if (compression == ImageType.JPG) {
			args = new String[]{"-m", "6", "-q", "90"};
		}
		return ImageUtil.runWebP(rawFile, args, out);
	}
}
