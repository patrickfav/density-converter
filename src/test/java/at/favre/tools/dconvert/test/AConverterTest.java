package at.favre.tools.dconvert.test;

import at.favre.tools.dconvert.arg.Arguments;
import at.favre.tools.dconvert.arg.EOutputCompressionMode;
import at.favre.tools.dconvert.arg.EPlatform;
import at.favre.tools.dconvert.arg.RoundingHandler;
import at.favre.tools.dconvert.converters.AndroidConverter;
import at.favre.tools.dconvert.converters.ConverterCallback;
import at.favre.tools.dconvert.converters.IOSConverter;
import at.favre.tools.dconvert.converters.IPlatformConverter;
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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.fail;

/**
 * Unit tests for {@link at.favre.tools.dconvert.converters.IPlatformConverter}
 */
public abstract class AConverterTest {
	protected static final float DEFAULT_SCALE = 3;
	protected static final int TIMEOUT_DELAY_SEC = 5;

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();
	protected File defaultDst;
	protected File defaultSrc;
	protected CountDownLatch countDownLatch;
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
		defaultDst = temporaryFolder.newFolder("android-converter-test", "out");
		defaultSrc = temporaryFolder.newFolder("android-converter-test", "src");
		converter = getType() == EPlatform.ANROID ? new AndroidConverter() : getType() == EPlatform.IOS ? new IOSConverter() : null;
		countDownLatch = new CountDownLatch(1);
		defaultCallback = new ConverterCallback() {
			@Override
			public void success(String log, List<File> compressedImages) {
				countDownLatch.countDown();
			}

			@Override
			public void failure(Exception e) {
				e.printStackTrace();
				fail("got error callback: " + e.getMessage());
			}
		};
	}

	@After
	public void tearDown() {
		defaultDst = defaultSrc = null;
		converter = null;
		countDownLatch = null;
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

	protected void defaultTest(List<File> files) throws Exception {
		test(new Arguments.Builder(defaultSrc, DEFAULT_SCALE).compression(EOutputCompressionMode.SAME_AS_INPUT_PREF_PNG, 0.5f)
				.dstFolder(defaultDst).platform(getType()).build(), files);
	}

	protected void test(Arguments arg, List<File> files) throws Exception {
		for (File fileToProcess : arg.filesToProcess) {
			converter.convert(fileToProcess, arg, defaultCallback);
		}
		countDownLatch.await(TIMEOUT_DELAY_SEC, TimeUnit.SECONDS);
		checkOutDir(arg.dst, arg, files, getType());
	}

	protected static List<File> copyToTestPath(File defaultSrc, String... resourceNames) throws Exception {
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

	protected static Dimension getScaledDimension(Arguments args, Dimension dimension, float scale) {
		double baseWidth = (double) dimension.width / args.scale;
		double baseHeight = (double) dimension.height / args.scale;

		return new Dimension((int) args.round(baseWidth * scale),
				(int) args.round(baseHeight * scale));
	}

}
