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

package at.favre.tools.dconvert.converters;

import at.favre.tools.dconvert.arg.Arguments;
import at.favre.tools.dconvert.arg.EPlatform;
import at.favre.tools.dconvert.arg.ImageType;
import at.favre.tools.dconvert.converters.descriptors.WindowsDensityDescriptor;
import at.favre.tools.dconvert.util.MiscUtil;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Needed info to convert for Android
 */
public class WindowsConverter extends APlatformConverter<WindowsDensityDescriptor> {

	@Override
	public List<WindowsDensityDescriptor> usedOutputDensities(Arguments arguments) {
		return getWindowsDescriptors();
	}

	public static List<WindowsDensityDescriptor> getWindowsDescriptors() {
		List<WindowsDensityDescriptor> list = new ArrayList<>();
		list.add(new WindowsDensityDescriptor(1, "100%", ".scale-100"));
		list.add(new WindowsDensityDescriptor(1.4f, "140%", ".scale-140"));
		list.add(new WindowsDensityDescriptor(1.8f, "180%", ".scale-180"));
		list.add(new WindowsDensityDescriptor(2.4f, "240%", ".scale-240"));
		return list;
	}

	@Override
	public String getConverterName() {
		return "windows-converter";
	}

	@Override
	public File createMainSubFolder(File destinationFolder, String targetImageFileName, Arguments arguments) {
		if (arguments.platform != EPlatform.WINDOWS) {
			destinationFolder = MiscUtil.createAndCheckFolder(new File(destinationFolder, "windows").getAbsolutePath());
		}
		return MiscUtil.createAndCheckFolder(new File(destinationFolder, "Assets").getAbsolutePath());
	}

	@Override
	public File createFolderForOutputFile(File mainSubFolder, WindowsDensityDescriptor density, Dimension dimension, String targetFileName, Arguments arguments) {
		return mainSubFolder;
	}

	@Override
	public String createDestinationFileNameWithoutExtension(WindowsDensityDescriptor density, Dimension dimension, String targetFileName, Arguments arguments) {
		return targetFileName + density.postFix;
	}

	@Override
	public void onPreExecute(File dstFolder, String targetFileName, List<WindowsDensityDescriptor> densityDescriptions, ImageType imageType, Arguments arguments) throws Exception {

	}

	@Override
	public void onPostExecute(Arguments arguments) {

	}
}