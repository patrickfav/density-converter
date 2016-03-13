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

import at.favre.tools.dconvert.arg.*;
import at.favre.tools.dconvert.converters.*;
import at.favre.tools.dconvert.util.ImageUtil;
import org.junit.*;
import org.junit.rules.TemporaryFolder;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Unit tests for {@link at.favre.tools.dconvert.converters.IPlatformConverter}
 */
public abstract class AConverterTest {
	protected static final float DEFAULT_SCALE = 3;

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();
	protected File defaultDst;
	protected File defaultSrc;
	protected IPlatformConverter converter;
	protected ConverterCallback defaultCallback;

	@BeforeClass
	public static void oneTimeSetUp() {

	}

	@AfterClass
	public static void oneTimeTearDown() {
	}

	@Before
	public void setUp() throws IOException {
		defaultSrc = temporaryFolder.newFolder("android-converter-test", "src");
		defaultDst = temporaryFolder.newFolder("android-converter-test", "out");
		converter = getType() == EPlatform.ANDROID ? new AndroidConverter() : getType() == EPlatform.IOS ? new IOSConverter() : getType() == EPlatform.WINDOWS ? new WindowsConverter() : null;
	}

	@After
	public void tearDown() {
		defaultDst = defaultSrc = null;
		converter = null;
		defaultCallback = null;
	}

	protected abstract EPlatform getType();

	protected abstract void checkOutDir(File dstDir, Arguments arguments, List<File> files, EPlatform type) throws IOException;

	@Test
	public void testSinglePng() throws Exception {
		List<File> files = copyToTestPath(defaultSrc, "png_example1_alpha_144.png");
		defaultTest(files);
	}

	@Test
	public void testSingleJpeg() throws Exception {
		List<File> files = copyToTestPath(defaultSrc, "jpg_example_1920.jpg");
		defaultTest(files);
	}

	@Test
	public void testSingleGif() throws Exception {
		List<File> files = copyToTestPath(defaultSrc, "gif_example_640.gif");
		defaultTest(files);
	}

	@Test
	public void testSingleBmp() throws Exception {
		List<File> files = copyToTestPath(defaultSrc, "bmp_example_256.bmp");
		defaultTest(files);
	}

	@Test
	public void testSingleTiff() throws Exception {
		List<File> files = copyToTestPath(defaultSrc, "tiff_example_256.tif");
		defaultTest(files);
	}

	@Test
	public void testSinglePsd() throws Exception {
		List<File> files = copyToTestPath(defaultSrc, "psd_example_827.psd");
		defaultTest(files);
	}

	@Test
	public void testSingleSvg() throws Exception {
		List<File> files = copyToTestPath(defaultSrc, "svg_example_512.svg");
		defaultTest(files);
	}

	@Test
	public void testRoundMode() throws Exception {
		List<File> files = copyToTestPath(defaultSrc, "png_example1_alpha_144.png");
		test(new Arguments.Builder(defaultSrc, 2.33f).dstFolder(defaultDst).scaleRoundingStragy(RoundingHandler.Strategy.FLOOR).platform(getType()).build(), files);
	}

	@Test
	public void testScaleWidthInDp() throws Exception {
		List<File> files = copyToTestPath(defaultSrc, "jpg_example_1920.jpg");
		test(new Arguments.Builder(defaultSrc, 24).dstFolder(defaultDst).scaleType(EScaleType.DP_WIDTH).platform(getType()).build(), files);
	}

	@Test
	public void testMultipleScaleWidthInDp() throws Exception {
		List<File> files = copyToTestPath(defaultSrc, "png_example1_alpha_144.png", "png_example4_500.png", "jpg_example_1920.jpg");
		test(new Arguments.Builder(defaultSrc, 48).dstFolder(defaultDst).scaleType(EScaleType.DP_WIDTH).platform(getType()).build(), files);
	}

	@Test
	public void testScaleHeightInDp() throws Exception {
		List<File> files = copyToTestPath(defaultSrc, "jpg_example_1920.jpg");
		test(new Arguments.Builder(defaultSrc, 128).dstFolder(defaultDst).scaleType(EScaleType.DP_HEIGHT).platform(getType()).build(), files);
	}

	@Test
	public void testMultipleScaleHeightInDp() throws Exception {
		List<File> files = copyToTestPath(defaultSrc, "png_example1_alpha_144.png", "png_example4_500.png", "jpg_example_1920.jpg");
		test(new Arguments.Builder(defaultSrc, 48).dstFolder(defaultDst).scaleType(EScaleType.DP_HEIGHT).platform(getType()).build(), files);
	}

	@Test
	public void testMultiplePng() throws Exception {
		List<File> files = copyToTestPath(defaultSrc, "png_example1_alpha_144.png", "png_example2_alpha_144.png", "png_example3_alpha_128.png");
		defaultTest(files);
	}

	@Test
	public void testPngAndJpegKeepCompressions() throws Exception {
		List<File> files = copyToTestPath(defaultSrc, "png_example2_alpha_144.png", "jpg_example_1920.jpg");
		defaultTest(files);
	}

	@Test
	public void testMixedCompressionsShouldBeMostlyPng() throws Exception {
		List<File> files = copyToTestPath(defaultSrc, "png_example2_alpha_144.png", "gif_example_640.gif", "psd_example_827.psd", "bmp_example_256.bmp", "jpg_example_1920.jpg");
		defaultTest(files);
	}

	@Test
	public void testMixedCompressionsShouldKeepCompressions() throws Exception {
		List<File> files = copyToTestPath(defaultSrc, "png_example2_alpha_144.png", "gif_example_640.gif", "tiff_example_256.tif", "bmp_example_256.bmp", "jpg_example_1920.jpg");
		test(new Arguments.Builder(defaultSrc, DEFAULT_SCALE).compression(EOutputCompressionMode.SAME_AS_INPUT_STRICT, 0.5f)
				.dstFolder(defaultDst).platform(getType()).build(), files);
	}

	@Test
	public void testMixedCompressionsShouldCreateJpg() throws Exception {
		List<File> files = copyToTestPath(defaultSrc, "png_example2_alpha_144.png", "gif_example_640.gif", "psd_example_827.psd", "bmp_example_256.bmp", "jpg_example_1920.jpg");
		test(new Arguments.Builder(defaultSrc, DEFAULT_SCALE).compression(EOutputCompressionMode.AS_JPG, 0.0f)
				.dstFolder(defaultDst).platform(getType()).build(), files);
	}

	@Test
	public void testMixedCompressionsShouldCreateJpgAndPng() throws Exception {
		List<File> files = copyToTestPath(defaultSrc, "png_example2_alpha_144.png", "gif_example_640.gif", "jpg_example_1920.jpg");
		test(new Arguments.Builder(defaultSrc, DEFAULT_SCALE).compression(EOutputCompressionMode.AS_JPG_AND_PNG, 0.0f)
				.dstFolder(defaultDst).platform(getType()).build(), files);
	}

	@Test
	public void testDryRun() throws Exception {
		List<File> files = copyToTestPath(defaultSrc, "png_example2_alpha_144.png", "gif_example_640.gif", "jpg_example_1920.jpg");
		test(new Arguments.Builder(defaultSrc, DEFAULT_SCALE).compression(EOutputCompressionMode.AS_JPG_AND_PNG, 0.0f)
				.dstFolder(defaultDst).platform(getType()).dryRun(true).build(), files);
	}

	protected void defaultTest(List<File> files) throws Exception {
		test(new Arguments.Builder(defaultSrc, DEFAULT_SCALE).compression(EOutputCompressionMode.SAME_AS_INPUT_PREF_PNG, 0.5f)
				.dstFolder(defaultDst).platform(getType()).build(), files);
	}

	protected void test(Arguments arg, List<File> files) throws Exception {
		for (File fileToProcess : arg.filesToProcess) {
			Result result = converter.convert(fileToProcess, arg);
			assertNull("should be no exception", result.exception);
		}

		if (arg.dryRun) {
			assertEquals("destination folder should be empty", 0, arg.dst.list().length);
		} else {
			checkOutDir(arg.dst, arg, files, getType());
		}
	}

	public static List<File> copyToTestPath(File defaultSrc, String... resourceNames) throws Exception {
		List<File> copiedFiles = new ArrayList<>();
		for (String resourceName : resourceNames) {
			File dstFile = new File(defaultSrc, resourceName);
			Files.copy(new File(AConverterTest.class.getClassLoader().getResource(resourceName).getFile()).toPath(), dstFile.toPath());
			copiedFiles.add(dstFile);
		}
		return copiedFiles;
	}

	protected static Map<File, Dimension> createDimensionMap(List<File> files) throws IOException {
		Map<File, Dimension> map = new HashMap<>();

		for (File file : files) {
			map.put(file, ImageUtil.getImageDimension(file));
		}
		return map;
	}

	protected static Dimension getScaledDimension(File srcFile, Arguments args, Dimension dimension, float scale) throws IOException {
		double baseWidth;
		double baseHeight;

		if (args.scaleType == EScaleType.DP_WIDTH) {
			Dimension srcDimension = ImageUtil.getImageDimension(srcFile);
			float scaleFactor = args.scale / (float) srcDimension.width;

			baseWidth = (int) args.round(args.scale);
			baseHeight = (int) args.round(scaleFactor * (float) srcDimension.height);
		} else if (args.scaleType == EScaleType.DP_HEIGHT) {
			Dimension srcDimension = ImageUtil.getImageDimension(srcFile);
			float scaleFactor = args.scale / (float) srcDimension.height;

			baseWidth = (int) args.round(scaleFactor * (float) srcDimension.width);
			baseHeight = (int) args.round(args.scale);
		} else {
			baseWidth = (double) dimension.width / args.scale;
			baseHeight = (double) dimension.height / args.scale;
		}

		return new Dimension((int) args.round(baseWidth * scale),
				(int) args.round(baseHeight * scale));
	}


	protected static class ImageInfo {
		public final File srcFile;
		public final String targetFileName;
		public final float scale;
		public boolean found = false;

		public ImageInfo(File srcFile, String targetFileName, float scale) {
			this.srcFile = srcFile;
			this.targetFileName = targetFileName;
			this.scale = scale;
		}
	}

}
