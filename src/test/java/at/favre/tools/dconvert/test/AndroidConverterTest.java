package at.favre.tools.dconvert.test;

import at.favre.tools.dconvert.arg.*;
import at.favre.tools.dconvert.converters.AndroidConverter;
import at.favre.tools.dconvert.converters.ConverterCallback;
import at.favre.tools.dconvert.util.ImageUtil;
import at.favre.tools.dconvert.util.MiscUtil;
import org.junit.*;
import org.junit.rules.TemporaryFolder;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Unit test of the {@link at.favre.tools.dconvert.converters.IPlatformConverter} for android
 */
public class AndroidConverterTest {
	private static final float DEFAULT_SCALE = 3;
	public static final int TIMEOUT_DELAY_SEC = 5;
	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	private File defaultDst;
	private File defaultSrc;
	private CountDownLatch countDownLatch;
	private AndroidConverter androidConverter;
	private ConverterCallback defaultCallback;

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
		androidConverter = new AndroidConverter();
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
		androidConverter = null;
		countDownLatch = null;
		defaultCallback = null;
	}

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
	public void testMipmapFolder() throws Exception {
		List<File> files = copyToTestPath(defaultSrc, "png_example1_alpha_144.png");
		test(new Arguments.Builder(defaultSrc, DEFAULT_SCALE).dstFolder(defaultDst).createMipMapInsteadOfDrawableDir(true).includeAndroidLdpiTvdpi(true).platform(EPlatform.ANROID).build(), files);
	}

	@Test
	public void testLdpiAndTvdpi() throws Exception {
		List<File> files = copyToTestPath(defaultSrc, "png_example1_alpha_144.png");
		test(new Arguments.Builder(defaultSrc, DEFAULT_SCALE).dstFolder(defaultDst).includeAndroidLdpiTvdpi(true).platform(EPlatform.ANROID).build(), files);
	}

	@Test
	public void testRoundMode() throws Exception {
		List<File> files = copyToTestPath(defaultSrc, "png_example1_alpha_144.png");
		test(new Arguments.Builder(defaultSrc, 2.33f).dstFolder(defaultDst).scaleRoundingStragy(RoundingHandler.Strategy.FLOOR).platform(EPlatform.ANROID).build(), files);
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
				.dstFolder(defaultDst).platform(EPlatform.ANROID).build(), files);
	}

	@Test
	public void testMixedCompressionsShouldCreateJpg() throws Exception {
		List<File> files = copyToTestPath(defaultSrc, "png_example2_alpha_144.png", "gif_example_640.gif", "psd_example_827.psd", "bmp_example_256.bmp", "jpg_example_1920.jpg");
		test(new Arguments.Builder(defaultSrc, DEFAULT_SCALE).compression(EOutputCompressionMode.AS_JPG, 0.0f)
				.dstFolder(defaultDst).platform(EPlatform.ANROID).build(), files);
	}

	@Test
	public void testMixedCompressionsShouldCreateJpgAndPng() throws Exception {
		List<File> files = copyToTestPath(defaultSrc, "png_example2_alpha_144.png", "gif_example_640.gif", "jpg_example_1920.jpg");
		test(new Arguments.Builder(defaultSrc, DEFAULT_SCALE).compression(EOutputCompressionMode.AS_JPG_AND_PNG, 0.0f)
				.dstFolder(defaultDst).platform(EPlatform.ANROID).build(), files);
	}

	private void defaultTest(List<File> files) throws Exception {
		test(new Arguments.Builder(defaultSrc, DEFAULT_SCALE).compression(EOutputCompressionMode.SAME_AS_INPUT_PREF_PNG, 0.5f)
				.dstFolder(defaultDst).platform(EPlatform.ANROID).build(), files);
	}

	private void test(Arguments arg, List<File> files) throws Exception {
		for (File fileToProcess : arg.filesToProcess) {
			androidConverter.convert(fileToProcess, arg, defaultCallback);
		}
		countDownLatch.await(TIMEOUT_DELAY_SEC, TimeUnit.SECONDS);
		checkOutDir(arg.dst, arg, files);
	}

	private static List<File> copyToTestPath(File defaultSrc, String... resourceNames) throws IOException {
		List<File> copiedFiles = new ArrayList<>();
		for (String resourceName : resourceNames) {
			File dstFile = new File(defaultSrc, resourceName);
			Files.copy(new File(AndroidConverterTest.class.getClassLoader().getResource(resourceName).getFile()).toPath(), dstFile.toPath());
			copiedFiles.add(dstFile);
		}
		return copiedFiles;
	}


	private static void checkOutDir(File dstDir, Arguments arguments, List<File> files) {
		List<DensityFolder> expectedDirs = new ArrayList<>();

		String prefix = arguments.createMipMapInsteadOfDrawableDir ? "mipmap" : "drawable";

		expectedDirs.add(new DensityFolder(prefix + "-mdpi", 1f));
		expectedDirs.add(new DensityFolder(prefix + "-hdpi", 1.5f));
		expectedDirs.add(new DensityFolder(prefix + "-xhdpi", 2f));
		expectedDirs.add(new DensityFolder(prefix + "-xxhdpi", 3f));
		expectedDirs.add(new DensityFolder(prefix + "-xxxhdpi", 4f));

		if (arguments.includeAndroidLdpiTvdpi) {
			expectedDirs.add(new DensityFolder(prefix + "-ldpi", 0.75f));
			expectedDirs.add(new DensityFolder(prefix + "-tvdpi", 1.33f));
		}

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
						for (File childFile : new File(dstDir, path).listFiles()) {
							if (expectedFile.targetFile.equals(childFile)) {
								expectedFile.found = true;
								Dimension expectedDimension = getScaledDimension(arguments, expectedFile.dimension, expectedDir.scaleFactor);
								assertEquals("dimensions should match", expectedDimension, ImageUtil.getImageDimension(childFile));
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

	private static Dimension getScaledDimension(Arguments args, Dimension dimension, float scale) {
		double baseWidth = (double) dimension.width / args.scrScale;
		double baseHeight = (double) dimension.height / args.scrScale;

		return new Dimension((int) args.round(baseWidth * scale),
				(int) args.round(baseHeight * scale));
	}

	private static List<ImageCheck> createExpectedFilesMap(Arguments arguments, File file, List<File> files) throws IOException {
		List<ImageCheck> expectedFiles = new ArrayList<>();

		for (File imageFile : files) {
			for (ImageType.ECompression compression : Arguments.getOutCompressionForType(arguments.outputCompressionMode, Arguments.getImageType(imageFile))) {
				expectedFiles.add(new ImageCheck(imageFile, new File(file, MiscUtil.getFileNameWithoutExtension(imageFile) + "." + compression.extension)));
			}
		}

		return expectedFiles;
	}

	private static class ImageCheck {
		public final File targetFile;
		public final Dimension dimension;
		public boolean found;

		public ImageCheck(File dstFile, File targetFile) throws IOException {
			this.targetFile = targetFile;
			this.dimension = ImageUtil.getImageDimension(dstFile);
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
