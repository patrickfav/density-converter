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

package at.favre.tools.dconvert.test;

import at.favre.tools.dconvert.arg.Arguments;
import at.favre.tools.dconvert.arg.EPlatform;
import at.favre.tools.dconvert.converters.WindowsConverter;
import at.favre.tools.dconvert.converters.descriptors.WindowsDensityDescriptor;
import at.favre.tools.dconvert.util.ImageUtil;
import at.favre.tools.dconvert.util.MiscUtil;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit test of the {@link at.favre.tools.dconvert.converters.IPlatformConverter} for ios
 */
public class WindowsConverterTest extends AConverterTest {
	@Override
	protected EPlatform getType() {
		return EPlatform.WINDOWS;
	}

	@Override
	protected void checkOutDir(File dstDir, Arguments arguments, List<File> files, EPlatform type) throws IOException {
		checkOutDirWindows(dstDir, arguments, files);
	}

	public static void checkOutDirWindows(File dstDir, Arguments arguments, List<File> files) throws IOException {
		dstDir = new File(dstDir, "Assets");

		Map<File, Dimension> dimensionMap = createDimensionMap(files);

		List<WindowsDensityDescriptor> densityDescriptors = WindowsConverter.getWindowsDescriptors();

		assertTrue("src files and dst folder count should match", dstDir.listFiles().length >= files.size());

		System.out.println("windows-convert " + files);

		List<ImageInfo> expectedFiles = new ArrayList<>();
		for (File srcImageFile : files) {
			for (WindowsDensityDescriptor descriptor : densityDescriptors) {
				expectedFiles.addAll(Arguments.getOutCompressionForType(arguments.outputCompressionMode, Arguments.getImageType(srcImageFile)).stream().map(compression -> new ImageInfo(srcImageFile, MiscUtil.getFileNameWithoutExtension(srcImageFile) + descriptor.postFix + "." + compression.extension, descriptor.scale)).collect(Collectors.toList()));
			}
		}

		for (File imageFile : dstDir.listFiles()) {
			for (ImageInfo expectedFile : expectedFiles) {
				if (expectedFile.targetFileName.equals(imageFile.getName())) {
					expectedFile.found = true;

					Dimension expectedDimension = getScaledDimension(expectedFile.srcFile, arguments, dimensionMap.get(expectedFile.srcFile), expectedFile.scale);
					assertEquals("dimensions should match", expectedDimension, ImageUtil.getImageDimension(imageFile));
				}
			}
		}

		for (ImageInfo expectedFile : expectedFiles) {
			assertTrue(expectedFile.targetFileName + " expected in folder " + dstDir, expectedFile.found);
		}

		System.out.println("found " + expectedFiles.size() + " files in " + dstDir);
	}

}
