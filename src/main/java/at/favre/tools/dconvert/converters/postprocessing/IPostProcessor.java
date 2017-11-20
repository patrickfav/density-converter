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

import at.favre.tools.dconvert.converters.Result;

import java.io.File;

/**
 * PostProcessor run after the main conversation on all files
 */
public interface IPostProcessor {
    String ORIG_POSTFIX = "_orig";

    /**
     * Will process the given file. It is not necessary to spawn another thread for exectution
     *
     * @param rawFile      to process
     * @param keepOriginal if true will not delete unprocessed file, but renames it to (filename)_orig.(extension)
     * @return optional log or output
     */
    Result process(File rawFile, boolean keepOriginal);

    /**
     * @return true if this processor is supported with the current setup (e.g. tool is set in PATH)
     */
    boolean isSupported();
}
