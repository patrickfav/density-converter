/*
 *  Copyright 2016 Patrick Favre-Bulle
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package at.favre.tools.dconvert.converters.postprocessing;

import at.favre.tools.dconvert.arg.Arguments;
import at.favre.tools.dconvert.arg.ImageType;
import at.favre.tools.dconvert.converters.Result;
import at.favre.tools.dconvert.util.MiscUtil;
import at.favre.tools.dconvert.util.PostProcessorUtil;

import java.io.File;
import java.util.Collections;

/**
 * Converts pngs/jpegs to lossless/lossy webp
 */
public class WebpProcessor extends APostProcessor {

    @Override
    public Result synchronizedProcess(File rawFile, boolean keepOriginal) {
        try {
            ImageType compression = Arguments.getImageType(rawFile);
            String[] additionalArgs;
            if (compression == ImageType.PNG || compression == ImageType.GIF) {
                additionalArgs = new String[]{"-lossless", "-alpha_filter", "best", "-m", "6"};
            } else if (compression == ImageType.JPG) {
                additionalArgs = new String[]{"-m", "6", "-q", "90"};
            } else {
                return null;
            }

            String[] finalArg = MiscUtil.concat(MiscUtil.concat(new String[]{"cwebp"}, additionalArgs), new String[]{"%%sourceFilePath%%", "-o", "%%outFilePath%%"});

            return PostProcessorUtil.runImageOptimizer(rawFile, compression, finalArg, keepOriginal, "webp");
        } catch (Exception e) {
            return new Result("could not execute post processor " + getClass().getSimpleName(), e, Collections.singletonList(rawFile));
        }
    }

    @Override
    public boolean isSupported() {
        return PostProcessorUtil.canRunCmd(new String[]{"cwebp", "-h"});
    }
}
