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

package at.favre.tools.converter.converters;

import at.favre.tools.converter.arg.Arguments;

import java.io.File;

/**
 * Defines how an image will be converted for a specific platform and densities
 */
public interface IPlatformConverter {

	/**
	 * Converts the given file to all needed densities
	 *
	 * @param destinationFolder   the (sub) folder to save output files
	 * @param srcImageFile        source image file to be used as base to scale
	 * @param arguments           all tool args
	 * @param callback            if not null need to be called if finished or failed
	 */
	void convert(File destinationFolder, File srcImageFile, Arguments arguments, ConverterCallback callback);
}
