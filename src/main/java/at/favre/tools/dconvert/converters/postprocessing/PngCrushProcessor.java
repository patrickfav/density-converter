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

import at.favre.tools.dconvert.arg.ImageType;
import at.favre.tools.dconvert.converters.Result;
import at.favre.tools.dconvert.util.MiscUtil;
import at.favre.tools.dconvert.util.PostProcessorUtil;

import java.io.File;
import java.util.Collections;

/**
 * Calls pngcrush on a file
 */
public class PngCrushProcessor extends APostProcessor {
    private static final String[] DEFAULT_ARGS = new String[]{"-rem", "alla", "-rem", "text", "-rem", "gAMA", "-rem", "cHRM", "-rem", "iCCP", "-rem", "sRGB"};
    private String[] additionalArgs;

    public PngCrushProcessor() {
        this(DEFAULT_ARGS);
    }

    public PngCrushProcessor(String[] additionalArgs) {
        this.additionalArgs = additionalArgs;
    }

    @Override
    public Result synchronizedProcess(File rawFile, boolean keepOriginal) {
        try {
            String[] args = MiscUtil.concat(MiscUtil.concat(new String[]{"pngcrush"}, additionalArgs), new String[]{"%%sourceFilePath%%", "%%outFilePath%%"});
            return PostProcessorUtil.runImageOptimizer(rawFile, ImageType.PNG, args, keepOriginal);
        } catch (Exception e) {
            return new Result("could not execute post processor " + getClass().getSimpleName(), e, Collections.singletonList(rawFile));
        }
    }

    @Override
    public boolean isSupported() {
        return PostProcessorUtil.canRunCmd(new String[]{"pngcrush", "-h"});
    }
}
