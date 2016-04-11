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
import at.favre.tools.dconvert.exceptions.InvalidArgumentException;
import at.favre.tools.dconvert.ui.CLIInterpreter;
import org.apache.tools.ant.types.Commandline;
import org.junit.*;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import static junit.framework.TestCase.assertEquals;

/**
 * Unit test of command line parser
 */
public class CLIParserTest {
    private static final String FOLDER1 = "test-out";
    private static final float DEFAULT_SCALE = 4;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private File defaultSrc;
    private String defaultCmd;

    @BeforeClass
    public static void oneTimeSetUp() {

    }

    @AfterClass
    public static void oneTimeTearDown() {
    }

    @Before
    public void setUp() throws IOException {
        defaultSrc = temporaryFolder.newFolder(FOLDER1);
        defaultCmd = "-" + CLIInterpreter.SOURCE_ARG + " \"" + defaultSrc.getAbsolutePath() + "\" -" + CLIInterpreter.SCALE_ARG + " " + DEFAULT_SCALE;
    }

    @After
    public void tearDown() {
        defaultSrc = null;
        defaultCmd = null;
    }

    @Test
    public void testSimpleUsage() throws Exception {
        float scale = 2f;
        check("-" + CLIInterpreter.SOURCE_ARG + " \"" + defaultSrc.getAbsolutePath() + "\" -" + CLIInterpreter.SCALE_ARG + " " + scale,
                new Arguments.Builder(defaultSrc, scale).build());
    }

    @Test
    public void testScales() throws Exception {
        for (Float scale : Arrays.asList(new Float[]{0.75f, 1f, 2f, 3f, 4f, 5f, 6f, 1.33f, 2.3936573f})) {
            check("-" + CLIInterpreter.SOURCE_ARG + " \"" + defaultSrc.getAbsolutePath() + "\"  -" + CLIInterpreter.SCALE_ARG + " " + scale,
                    new Arguments.Builder(defaultSrc, scale).build());
        }
    }

    @Test
    public void testScalesWidthInDp() throws Exception {
        for (Integer scale : Arrays.asList(new Integer[]{1, 12, 24, 48, 106, 33, 500, 96, 256, 480})) {
            check("-" + CLIInterpreter.SOURCE_ARG + " \"" + defaultSrc.getAbsolutePath() + "\"  -" + CLIInterpreter.SCALE_ARG + " " + scale + "dp",
                    new Arguments.Builder(defaultSrc, scale).scaleMode(EScaleMode.DP_WIDTH).build());
        }
    }

    @Test(expected = InvalidArgumentException.class)
    public void test0ScalesWidthInDp() throws Exception {
        check("-" + CLIInterpreter.SOURCE_ARG + " \"" + defaultSrc.getAbsolutePath() + "\"  -" + CLIInterpreter.SCALE_ARG + " " + 0 + "dp",
                new Arguments.Builder(defaultSrc, 0).scaleMode(EScaleMode.DP_WIDTH).build());
    }

    @Test(expected = InvalidArgumentException.class)
    public void test9999ScalesWidthInDp() throws Exception {
        check("-" + CLIInterpreter.SOURCE_ARG + " \"" + defaultSrc.getAbsolutePath() + "\"  -" + CLIInterpreter.SCALE_ARG + " " + 9999 + "dp",
                new Arguments.Builder(defaultSrc, 9999).scaleMode(EScaleMode.DP_WIDTH).build());
    }

    @Test
    public void testScalesHeightInDp() throws Exception {
        for (Integer scale : Arrays.asList(new Integer[]{1, 12, 24, 48, 106, 33, 500, 96, 256, 480})) {
            check("-" + CLIInterpreter.SOURCE_ARG + " \"" + defaultSrc.getAbsolutePath() + "\" -" + CLIInterpreter.SCALE_IS_HEIGHT_DP_ARG + "  -" + CLIInterpreter.SCALE_ARG + " " + scale + "dp",
                    new Arguments.Builder(defaultSrc, scale).scaleMode(EScaleMode.DP_HEIGHT).build());
        }
    }

    @Test(expected = InvalidArgumentException.class)
    public void testScale0fShouldFail() throws Exception {
        check("-" + CLIInterpreter.SOURCE_ARG + " \"" + defaultSrc.getAbsolutePath() + "\"  -" + CLIInterpreter.SCALE_ARG + " " + 0f,
                new Arguments.Builder(defaultSrc, 0f).build());
    }

    @Test(expected = InvalidArgumentException.class)
    public void testScale100ShouldFail() throws Exception {
        check("-" + CLIInterpreter.SOURCE_ARG + " \"" + defaultSrc.getAbsolutePath() + "\"  -" + CLIInterpreter.SCALE_ARG + " " + 100f,
                new Arguments.Builder(defaultSrc, 100f).build());
    }

    @Test
    public void testSourceDirectories() throws Exception {
        File[] srcDirs = new File[]{temporaryFolder.newFolder("muh"), temporaryFolder.newFolder("android"), temporaryFolder.newFolder("out"),
                temporaryFolder.newFolder("layer1", "layer2"), temporaryFolder.newFolder("user", "Project", "su_Bf4-ldr")};

        for (File srcFolder : Arrays.asList(srcDirs)) {
            check("-" + CLIInterpreter.SOURCE_ARG + " \"" + srcFolder.getAbsolutePath() + "\" -" + CLIInterpreter.SCALE_ARG + " " + DEFAULT_SCALE,
                    new Arguments.Builder(srcFolder, DEFAULT_SCALE).build());
        }
    }

    @Test
    public void testDestDirectories() throws Exception {
        File[] dstDirs = new File[]{temporaryFolder.newFolder("simpleOut"), temporaryFolder.newFolder("ios"), temporaryFolder.newFolder("in"),
                temporaryFolder.newFolder("res", "drawable-xxhdpi"), temporaryFolder.newFolder("user", "Project", "su_Bf4-ldr")};

        for (File dstFolder : Arrays.asList(dstDirs)) {
            check(defaultCmd + " -" + CLIInterpreter.DST_ARG + " \"" + dstFolder.getAbsolutePath() + "\"",
                    new Arguments.Builder(defaultSrc, DEFAULT_SCALE).dstFolder(dstFolder).build());
        }
    }

    @Test
    public void testPlatforms() throws Exception {
        check(defaultCmd + " -" + CLIInterpreter.PLATFORM_ARG + " all", new Arguments.Builder(defaultSrc, DEFAULT_SCALE).platform(EPlatform.getAll()).build());
        check(defaultCmd + " -" + CLIInterpreter.PLATFORM_ARG + " android", new Arguments.Builder(defaultSrc, DEFAULT_SCALE).platform(Collections.singleton(EPlatform.ANDROID)).build());
        check(defaultCmd + " -" + CLIInterpreter.PLATFORM_ARG + " ios", new Arguments.Builder(defaultSrc, DEFAULT_SCALE).platform(Collections.singleton(EPlatform.IOS)).build());
        check(defaultCmd + " -" + CLIInterpreter.PLATFORM_ARG + " win", new Arguments.Builder(defaultSrc, DEFAULT_SCALE).platform(Collections.singleton(EPlatform.WINDOWS)).build());
        check(defaultCmd + " -" + CLIInterpreter.PLATFORM_ARG + " web", new Arguments.Builder(defaultSrc, DEFAULT_SCALE).platform(Collections.singleton(EPlatform.WEB)).build());
    }

    @Test
    public void testScalingAlgorithms() throws Exception {
        for (EScalingAlgorithm algorithm : EScalingAlgorithm.getAllEnabled()) {
            if (algorithm.getSupportedForType().contains(EScalingAlgorithm.Type.DOWNSCALING)) {
                check(defaultCmd + " -" + CLIInterpreter.DOWNSCALING_ALGO_ARG + " " + algorithm.getName(), new Arguments.Builder(defaultSrc, DEFAULT_SCALE).downScaleAlgorithm(algorithm).build());
            }
            if (algorithm.getSupportedForType().contains(EScalingAlgorithm.Type.UPSCALING)) {
                check(defaultCmd + " -" + CLIInterpreter.UPSCALING_ALGO_ARG + " " + algorithm.getName(), new Arguments.Builder(defaultSrc, DEFAULT_SCALE).upScaleAlgorithm(algorithm).build());
            }
        }
    }

    @Test
    public void testOutCompressions() throws Exception {
        check(defaultCmd + " -" + CLIInterpreter.OUT_COMPRESSION_ARG + " jpg", new Arguments.Builder(defaultSrc, DEFAULT_SCALE).compression(EOutputCompressionMode.AS_JPG).build());
        check(defaultCmd + " -" + CLIInterpreter.OUT_COMPRESSION_ARG + " png", new Arguments.Builder(defaultSrc, DEFAULT_SCALE).compression(EOutputCompressionMode.AS_PNG).build());
        check(defaultCmd + " -" + CLIInterpreter.OUT_COMPRESSION_ARG + " gif", new Arguments.Builder(defaultSrc, DEFAULT_SCALE).compression(EOutputCompressionMode.AS_GIF).build());
        check(defaultCmd + " -" + CLIInterpreter.OUT_COMPRESSION_ARG + " bmp", new Arguments.Builder(defaultSrc, DEFAULT_SCALE).compression(EOutputCompressionMode.AS_BMP).build());
        check(defaultCmd + " -" + CLIInterpreter.OUT_COMPRESSION_ARG + " png+jpg", new Arguments.Builder(defaultSrc, DEFAULT_SCALE).compression(EOutputCompressionMode.AS_JPG_AND_PNG).build());
    }

    @Test
    public void testCompressionQuality() throws Exception {
        for (Float compression : Arrays.asList(new Float[]{0.0f, 0.1f, 0.05f, 1.0f, 0.5f, 0.7f, 0.8f, 0.999f, 0.0001f})) {
            check(defaultCmd + " -" + CLIInterpreter.OUT_COMPRESSION_ARG + " jpg" + " -" + CLIInterpreter.COMPRESSION_QUALITY_ARG + " " + compression, new Arguments.Builder(defaultSrc, DEFAULT_SCALE).compression(EOutputCompressionMode.AS_JPG, compression).build());
        }
    }

    @Test(expected = InvalidArgumentException.class)
    public void testCompressionQualityShouldFail() throws Exception {
        check(defaultCmd + " -" + CLIInterpreter.OUT_COMPRESSION_ARG + " jpg" + " -" + CLIInterpreter.COMPRESSION_QUALITY_ARG + " " + 1.1f,
                new Arguments.Builder(defaultSrc, DEFAULT_SCALE).compression(EOutputCompressionMode.AS_JPG, 1.1f).build());
    }

    @Test
    public void testThreadCounts() throws Exception {
        for (Integer threadCount : Arrays.asList(new Integer[]{1, 2, 3, 4, 5, 6, 7, 8})) {
            check(defaultCmd + " -" + CLIInterpreter.THREADS_ARG + " " + threadCount, new Arguments.Builder(defaultSrc, DEFAULT_SCALE).threadCount(threadCount).build());
        }
    }

    @Test(expected = InvalidArgumentException.class)
    public void testThreadCount0ShouldFail() throws Exception {
        check(defaultCmd + " -" + CLIInterpreter.THREADS_ARG + " " + 0, new Arguments.Builder(defaultSrc, DEFAULT_SCALE).threadCount(0).build());
    }

    @Test
    public void testRoundingModes() throws Exception {
        check(defaultCmd + " -" + CLIInterpreter.ROUNDING_MODE_ARG + " round", new Arguments.Builder(defaultSrc, DEFAULT_SCALE).scaleRoundingStragy(RoundingHandler.Strategy.ROUND_HALF_UP).build());
        check(defaultCmd + " -" + CLIInterpreter.ROUNDING_MODE_ARG + " floor", new Arguments.Builder(defaultSrc, DEFAULT_SCALE).scaleRoundingStragy(RoundingHandler.Strategy.FLOOR).build());
        check(defaultCmd + " -" + CLIInterpreter.ROUNDING_MODE_ARG + " ceil", new Arguments.Builder(defaultSrc, DEFAULT_SCALE).scaleRoundingStragy(RoundingHandler.Strategy.CEIL).build());
    }

    @Test
    public void testFlags() throws Exception {
        check(defaultCmd + " -skipUpscaling", new Arguments.Builder(defaultSrc, DEFAULT_SCALE).skipUpscaling(true).build());
        check(defaultCmd + " -" + CLIInterpreter.SKIP_EXISTING_ARG, new Arguments.Builder(defaultSrc, DEFAULT_SCALE).skipExistingFiles(true).build());
        check(defaultCmd + " -androidIncludeLdpiTvdpi", new Arguments.Builder(defaultSrc, DEFAULT_SCALE).includeAndroidLdpiTvdpi(true).build());
        check(defaultCmd + " -" + CLIInterpreter.VERBOSE_ARG, new Arguments.Builder(defaultSrc, DEFAULT_SCALE).verboseLog(true).build());
        check(defaultCmd + " -haltOnError", new Arguments.Builder(defaultSrc, DEFAULT_SCALE).haltOnError(true).build());
        check(defaultCmd + " -androidMipmapInsteadOfDrawable", new Arguments.Builder(defaultSrc, DEFAULT_SCALE).createMipMapInsteadOfDrawableDir(true).build());
        check(defaultCmd + " -antiAliasing", new Arguments.Builder(defaultSrc, DEFAULT_SCALE).antiAliasing(true).build());
        check(defaultCmd + " -postProcessorPngCrush", new Arguments.Builder(defaultSrc, DEFAULT_SCALE).enablePngCrush(true).build());
        check(defaultCmd + " -postProcessorWebp", new Arguments.Builder(defaultSrc, DEFAULT_SCALE).postConvertWebp(true).build());
        check(defaultCmd + " -postProcessorMozJpeg", new Arguments.Builder(defaultSrc, DEFAULT_SCALE).enableMozJpeg(true).build());
        check(defaultCmd + " -keepOriginalPostProcessedFiles", new Arguments.Builder(defaultSrc, DEFAULT_SCALE).keepUnoptimizedFilesPostProcessor(true).build());
        check(defaultCmd + " -iosCreateImagesetFolders", new Arguments.Builder(defaultSrc, DEFAULT_SCALE).iosCreateImagesetFolders(true).build());
        check(defaultCmd + " -clean", new Arguments.Builder(defaultSrc, DEFAULT_SCALE).clearDirBeforeConvert(true).build());
    }

    @Test
    public void testFlagsCombinations() throws Exception {
        check(defaultCmd + " -skipUpscaling -haltOnError -postProcessorPngCrush", new Arguments.Builder(defaultSrc, DEFAULT_SCALE).haltOnError(true).enablePngCrush(true).skipUpscaling(true).build());
        check(defaultCmd + " -antiAliasing -androidIncludeLdpiTvdpi", new Arguments.Builder(defaultSrc, DEFAULT_SCALE).antiAliasing(true).includeAndroidLdpiTvdpi(true).build());
        check(defaultCmd + " -postProcessorWebp -" + CLIInterpreter.VERBOSE_ARG + " -skipUpscaling -antiAliasing", new Arguments.Builder(defaultSrc, DEFAULT_SCALE).antiAliasing(true).postConvertWebp(true).verboseLog(true).skipUpscaling(true).build());
    }

    @Test
    public void testDryRun() throws Exception {
        check(defaultCmd + " -dryRun", new Arguments.Builder(defaultSrc, DEFAULT_SCALE).dryRun(true).build());
    }

    private static void check(String cmd, Arguments ref) {
        Arguments arg = CLIInterpreter.parse(asArgArray(cmd));
        assertEquals("should create same args", ref, arg);
        System.out.println("command line: " + cmd);
        System.out.println("resulting arg: " + arg);
    }

    public static String[] asArgArray(String cmd) {
        return Commandline.translateCommandline(cmd);
    }
}
