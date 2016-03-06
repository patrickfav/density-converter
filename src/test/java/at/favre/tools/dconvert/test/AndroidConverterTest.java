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
import at.favre.tools.dconvert.arg.ImageType;
import at.favre.tools.dconvert.converters.AndroidConverter;
import at.favre.tools.dconvert.util.ImageUtil;
import at.favre.tools.dconvert.util.MiscUtil;
import org.junit.Test;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * Unit test of the {@link at.favre.tools.dconvert.converters.IPlatformConverter} for android
 */
public class AndroidConverterTest extends AConverterTest {

	@Test
	public void testMipmapFolder() throws Exception {
		List<File> files = copyToTestPath(defaultSrc, "png_example1_alpha_144.png");
		test(new Arguments.Builder(defaultSrc, DEFAULT_SCALE).dstFolder(defaultDst).createMipMapInsteadOfDrawableDir(true).includeAndroidLdpiTvdpi(true).platform(getType()).build(), files);
	}

	@Test
	public void testLdpiAndTvdpi() throws Exception {
		List<File> files = copyToTestPath(defaultSrc, "png_example1_alpha_144.png");
		test(new Arguments.Builder(defaultSrc, DEFAULT_SCALE).dstFolder(defaultDst).includeAndroidLdpiTvdpi(true).platform(getType()).build(), files);
	}

	@Override
	protected EPlatform getType() {
		return EPlatform.ANROID;
	}

	@Override
	protected void checkOutDir(File dstDir, Arguments arguments, List<File> files, EPlatform type) throws IOException {
		checkOutDirAndroid(dstDir, arguments, files);
	}

	private static void checkOutDirAndroid(File dstDir, Arguments arguments, List<File> files) throws IOException {
		Map<File, Dimension> dimensionMap = createDimensionMap(files);

		List<DensityFolder> expectedDirs = new ArrayList<>();

		String prefix = arguments.createMipMapInsteadOfDrawableDir ? "mipmap" : "drawable";

		expectedDirs.addAll(AndroidConverter.getAndroidDensityDescriptors(arguments).stream().map(
				androidDensityDescriptor -> new DensityFolder(prefix + androidDensityDescriptor.folderName.replace("drawable", ""), androidDensityDescriptor.scale)).collect(Collectors.toList()));

		assertFalse("expected dirs should not be empty", expectedDirs.isEmpty());
		assertFalse("output dir should not be empty", dstDir.list().length == 0);

		System.out.println("Android-convert " + files);

		for (String path : dstDir.list()) {
			expectedDirs.stream().filter(expectedDir -> expectedDir.folderName.equals(path)).forEach(expectedDir -> {
				try {
					expectedDir.found = true;

					List<ImageCheck> expectedFiles = createExpectedFilesMap(arguments, new File(dstDir, path), files);

					assertTrue("files count should match input", files.isEmpty() == expectedFiles.isEmpty());

					for (ImageCheck expectedFile : expectedFiles) {
						for (File imageFile : new File(dstDir, path).listFiles()) {
							if (expectedFile.targetFile.equals(imageFile)) {
								expectedFile.found = true;
								Dimension expectedDimension = getScaledDimension(expectedFile.srcFile, arguments, dimensionMap.get(expectedFile.srcFile), expectedDir.scaleFactor);
								assertEquals("dimensions should match", expectedDimension, ImageUtil.getImageDimension(imageFile));
							}
						}
					}

					for (ImageCheck expectedFile : expectedFiles) {
						assertTrue(expectedFile.targetFile + " file should be generated in path", expectedFile.found);
					}
					System.out.print("found " + expectedFiles.size() + " files in " + expectedDir.folderName + ", ");
				} catch (Exception e) {
					fail();
					e.printStackTrace();
				}
			});

		}

		for (DensityFolder expectedDir : expectedDirs) {
			assertTrue(expectedDir.folderName + " should be generated in path", expectedDir.found);
		}

		System.out.println();
	}

	private static List<ImageCheck> createExpectedFilesMap(Arguments arguments, File file, List<File> files) throws IOException {
		List<ImageCheck> expectedFiles = new ArrayList<>();

		for (File srcImageFile : files) {
			for (ImageType.ECompression compression : Arguments.getOutCompressionForType(arguments.outputCompressionMode, Arguments.getImageType(srcImageFile))) {
				expectedFiles.add(new ImageCheck(srcImageFile, new File(file, MiscUtil.getFileNameWithoutExtension(srcImageFile) + "." + compression.extension)));
			}
		}

		return expectedFiles;
	}

	private static class ImageCheck {
		public final File srcFile;
		public final File targetFile;
		public boolean found;

		public ImageCheck(File srcFile, File targetFile) throws IOException {
			this.srcFile = srcFile;
			this.targetFile = targetFile;
		}
	}

	private static class DensityFolder {
		public final String folderName;
		public final float scaleFactor;
		public boolean found;

		public DensityFolder(String folderName, float scaleFactor) {
			this.folderName = folderName;
			this.scaleFactor = scaleFactor;
		}
	}

}
