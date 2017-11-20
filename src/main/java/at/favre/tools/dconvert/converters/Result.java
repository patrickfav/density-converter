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

package at.favre.tools.dconvert.converters;

import java.io.File;
import java.util.List;

/**
 * Wrapper for a result
 */
public class Result {
    public final Exception exception;
    public final List<File> processedFiles;
    public final String log;

    public Result(String log, Exception exception, List<File> processedFiles) {
        this.log = log;
        this.exception = exception;
        this.processedFiles = processedFiles;
    }

    public Result(String log, List<File> processedFiles) {
        this(log, null, processedFiles);
    }
}
