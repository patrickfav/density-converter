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
import at.favre.tools.dconvert.converters.ConverterCallback;
import at.favre.tools.dconvert.converters.IPlatformConverter;
import at.favre.tools.dconvert.converters.Result;
import at.favre.tools.dconvert.converters.descriptors.PostfixDescriptor;
import at.favre.tools.dconvert.util.ImageUtil;
import at.favre.tools.dconvert.util.MiscUtil;
import org.junit.*;
import org.junit.rules.TemporaryFolder;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link at.favre.tools.dconvert.converters.IPlatformConverter}
 */
public abstract class AConverterTest {
    static final float DEFAULT_SCALE = 3;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();
    File defaultDst;
    File defaultSrc;
    IPlatformConverter converter;
    ConverterCallback defaultCallback;

    @BeforeClass
    public static void oneTimeSetUp() {

    }

    @AfterClass
    public static void oneTimeTearDown() {
    }

    @Before
    public void setUp() throws IOException {
        defaultSrc = temporaryFolder.newFolder("converter-test", "src");
        defaultDst = temporaryFolder.newFolder("converter-test", "out");
        converter = getType().getConverter();
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
        test(new Arguments.Builder(defaultSrc, 2.33f).dstFolder(defaultDst).scaleRoundingStragy(RoundingHandler.Strategy.FLOOR).platform(Collections.singleton(getType())).build(), files);
    }

    @Test
    public void testScaleWidthInDp() throws Exception {
        List<File> files = copyToTestPath(defaultSrc, "jpg_example_1920.jpg");
        test(new Arguments.Builder(defaultSrc, 24).dstFolder(defaultDst).scaleMode(EScaleMode.DP_WIDTH).platform(Collections.singleton(getType())).build(), files);
    }

    @Test
    public void testMultipleScaleWidthInDp() throws Exception {
        List<File> files = copyToTestPath(defaultSrc, "png_example1_alpha_144.png", "png_example4_500.png", "jpg_example_1920.jpg");
        test(new Arguments.Builder(defaultSrc, 48).dstFolder(defaultDst).scaleMode(EScaleMode.DP_WIDTH).platform(Collections.singleton(getType())).build(), files);
    }

    @Test
    public void testScaleHeightInDp() throws Exception {
        List<File> files = copyToTestPath(defaultSrc, "jpg_example_1920.jpg");
        test(new Arguments.Builder(defaultSrc, 128).dstFolder(defaultDst).scaleMode(EScaleMode.DP_HEIGHT).platform(Collections.singleton(getType())).build(), files);
    }

    @Test
    public void testMultipleScaleHeightInDp() throws Exception {
        List<File> files = copyToTestPath(defaultSrc, "png_example1_alpha_144.png", "png_example4_500.png", "jpg_example_1920.jpg");
        test(new Arguments.Builder(defaultSrc, 48).dstFolder(defaultDst).scaleMode(EScaleMode.DP_HEIGHT).platform(Collections.singleton(getType())).build(), files);
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
                .dstFolder(defaultDst).platform(Collections.singleton(getType())).build(), files);
    }

    @Test
    public void testMixedCompressionsShouldCreateJpg() throws Exception {
        List<File> files = copyToTestPath(defaultSrc, "png_example2_alpha_144.png", "gif_example_640.gif", "psd_example_827.psd", "bmp_example_256.bmp", "jpg_example_1920.jpg");
        test(new Arguments.Builder(defaultSrc, DEFAULT_SCALE).compression(EOutputCompressionMode.AS_JPG, 0.0f)
                .dstFolder(defaultDst).platform(Collections.singleton(getType())).build(), files);
    }

    @Test
    public void testMixedCompressionsShouldCreateJpgAndPng() throws Exception {
        List<File> files = copyToTestPath(defaultSrc, "png_example2_alpha_144.png", "gif_example_640.gif", "jpg_example_1920.jpg");
        test(new Arguments.Builder(defaultSrc, DEFAULT_SCALE).compression(EOutputCompressionMode.AS_JPG_AND_PNG, 0.0f)
                .dstFolder(defaultDst).platform(Collections.singleton(getType())).build(), files);
    }

    @Test
    public void testDryRun() throws Exception {
        List<File> files = copyToTestPath(defaultSrc, "png_example2_alpha_144.png", "gif_example_640.gif", "jpg_example_1920.jpg");
        test(new Arguments.Builder(defaultSrc, DEFAULT_SCALE).compression(EOutputCompressionMode.AS_JPG_AND_PNG, 0.0f)
                .dstFolder(defaultDst).platform(Collections.singleton(getType())).dryRun(true).build(), files);
    }

    protected void defaultTest(List<File> files) throws Exception {
        test(new Arguments.Builder(defaultSrc, DEFAULT_SCALE).compression(EOutputCompressionMode.SAME_AS_INPUT_PREF_PNG, 0.5f)
                .dstFolder(defaultDst).platform(Collections.singleton(getType())).build(), files);
    }

    protected void test(Arguments arg, List<File> files) throws Exception {
        for (File fileToProcess : arg.filesToProcess) {
            Result result = converter.convert(fileToProcess, arg);
            assertNull("should be no exception: " + result.exception, result.exception);
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

    public static void checkMultiPlatformConvert(File dst, Arguments arg, List<File> files) throws IOException {
        AndroidConverterTest.checkOutDirAndroid(new File(dst, "android"), arg, files);
        IOSConverterTest.checkOutDirIos(new File(dst, "ios"), arg, files);
        WindowsConverterTest.checkOutDirWindows(new File(dst, "windows"), arg, files);
        WebConverterTest.checkOutDirWeb(new File(dst, "web"), arg, files);
    }

    protected static Map<File, Dimension> createDimensionMap(List<File> files) throws IOException {
        Map<File, Dimension> map = new HashMap<>();

        for (File file : files) {
            map.put(file, ImageUtil.getImageDimension(file));
        }
        return map;
    }

    protected static Dimension getScaledDimension(File srcFile, Arguments args, Dimension dimension, float scale, boolean isNinePatch) throws IOException {
        double baseWidth;
        double baseHeight;

        if (args.scaleMode == EScaleMode.DP_WIDTH) {
            Dimension srcDimension = ImageUtil.getImageDimension(srcFile);
            float scaleFactor = args.scale / (float) srcDimension.width;

            baseWidth = (int) args.round(args.scale);
            baseHeight = (int) args.round(scaleFactor * (float) srcDimension.height);
        } else if (args.scaleMode == EScaleMode.DP_HEIGHT) {
            Dimension srcDimension = ImageUtil.getImageDimension(srcFile);
            float scaleFactor = args.scale / (float) srcDimension.height;

            baseWidth = (int) args.round(scaleFactor * (float) srcDimension.width);
            baseHeight = (int) args.round(args.scale);
        } else {
            baseWidth = (double) dimension.width / args.scale;
            baseHeight = (double) dimension.height / args.scale;
        }

        if (isNinePatch) {
            return new Dimension((int) args.round(((baseWidth + 1) * scale) + 2),
                    (int) args.round(((baseHeight + 1) * scale) + 2));
        } else {
            return new Dimension((int) args.round(baseWidth * scale),
                    (int) args.round(baseHeight * scale));
        }
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

    public static void checkOutDirPostfixDescr(File dstRootDir, Arguments arguments, List<File> files, List<PostfixDescriptor> densityDescriptors) throws IOException {
        Map<File, Dimension> dimensionMap = createDimensionMap(files);

        if (!files.isEmpty()) {
            assertTrue("src files and dst folder count should match", dstRootDir.listFiles().length >= files.size());

            List<ImageInfo> expectedFiles = new ArrayList<>();
            for (File srcImageFile : files) {
                for (PostfixDescriptor descriptor : densityDescriptors) {
                    expectedFiles.addAll(Arguments.getOutCompressionForType(arguments.outputCompressionMode, Arguments.getImageType(srcImageFile))
                            .stream()
                            .map(compression -> new ImageInfo(srcImageFile, MiscUtil.getFileNameWithoutExtension(srcImageFile) + descriptor.postFix + "." + compression.extension, descriptor.scale))
                            .collect(Collectors.toList()));
                }
            }

            for (File imageFile : dstRootDir.listFiles()) {
                for (ImageInfo expectedFile : expectedFiles) {
                    if (expectedFile.targetFileName.equals(imageFile.getName())) {
                        expectedFile.found = true;

                        Dimension expectedDimension = getScaledDimension(expectedFile.srcFile, arguments, dimensionMap.get(expectedFile.srcFile), expectedFile.scale, false);
                        assertEquals("dimensions should match", expectedDimension, ImageUtil.getImageDimension(imageFile));
                    }
                }
            }

            for (ImageInfo expectedFile : expectedFiles) {
                assertTrue(expectedFile.targetFileName + " expected in folder " + dstRootDir, expectedFile.found);
            }

            System.out.println("found " + expectedFiles.size() + " files in " + dstRootDir);
        } else {
            assertTrue(dstRootDir.list() == null || dstRootDir.list().length == 0);
        }
    }
}
