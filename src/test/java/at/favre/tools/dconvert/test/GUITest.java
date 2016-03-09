package at.favre.tools.dconvert.test;

import at.favre.tools.dconvert.arg.*;
import at.favre.tools.dconvert.test.helper.TestPreferenceStore;
import at.favre.tools.dconvert.ui.GUI;
import at.favre.tools.dconvert.ui.GUIController;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.testfx.framework.junit.ApplicationTest;

import java.io.File;
import java.util.Locale;

import static org.junit.Assert.assertEquals;

/**
 * Tests GUI
 */
public class GUITest extends ApplicationTest {
	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();
	private GUIController controller;
	private Arguments.Builder defaultBuilder;
	private File defaultSrcFolder;

	@BeforeClass
	public static void setupSpec() throws Exception {

		if (true) {
			System.setProperty("testfx.robot", "glass");
			System.setProperty("testfx.headless", "true");
			System.setProperty("prism.order", "sw");
			System.setProperty("prism.text", "t2k");
			System.setProperty("java.awt.headless", "true");
		}
	}

	@Override
	public void start(Stage stage) throws Exception {
		controller = GUI.setup(stage, new TestPreferenceStore());
		stage.show();
	}

	@Before
	public void setUp() throws Exception {
		defaultSrcFolder = temporaryFolder.newFolder();
		defaultBuilder = new Arguments.Builder(defaultSrcFolder, Arguments.DEFAULT_SCALE).verboseLog(true);
		controller.setSrcForTest(defaultSrcFolder);
		assertEquals("default should match", defaultBuilder.build(), controller.getFromUI(false));
	}

	@After
	public void tearDown() throws Exception {
		clickOn("#btnReset");
		assertEquals("should be default after reset", new Arguments.Builder(new File(""), Arguments.DEFAULT_SCALE).skipParamValidation(true).verboseLog(true).build(), controller.getFromUI(true));
	}

	@Test
	public void testScaleTypes() throws Exception {
		clickOn("#rbDpWidth").clickOn("#textFieldDp").write("48");
		assertEquals("arguments should match",
				new Arguments.Builder(defaultSrcFolder, 48).scaleType(EScaleType.DP_WIDTH).verboseLog(true).build(), controller.getFromUI(false));
		clickOn("#rbDpHeight").clickOn("#textFieldDp").write("1");
		assertEquals("arguments should match",
				new Arguments.Builder(defaultSrcFolder, 148).scaleType(EScaleType.DP_HEIGHT).verboseLog(true).build(), controller.getFromUI(false));
		clickOn("#rbFactor").clickOn("#scaleSlider");
		assertEquals("arguments should match",
				new Arguments.Builder(defaultSrcFolder, 3).scaleType(EScaleType.FACTOR).verboseLog(true).build(), controller.getFromUI(false));
	}

	@Test
	public void testPostProcessors() throws Exception {
		clickOn("#cbPostConvertWebp");
		assertEquals("arguments should match", defaultBuilder.postConvertWebp(true).build(), controller.getFromUI(false));
		clickOn("#cbPostConvertWebp");
		assertEquals("arguments should match", defaultBuilder.postConvertWebp(false).build(), controller.getFromUI(false));
		clickOn("#chEnablePngCrush");
		assertEquals("arguments should match", defaultBuilder.enablePngCrush(true).build(), controller.getFromUI(false));
		clickOn("#chEnablePngCrush");
		assertEquals("arguments should match", defaultBuilder.enablePngCrush(false).build(), controller.getFromUI(false));
	}

	@Test
	public void testCBOptions() throws Exception {
		clickOn("#cbMipmapInsteadDrawable");
		assertEquals("arguments should match", defaultBuilder.createMipMapInsteadOfDrawableDir(true).build(), controller.getFromUI(false));
		clickOn("#cbMipmapInsteadDrawable");
		assertEquals("arguments should match", defaultBuilder.createMipMapInsteadOfDrawableDir(false).build(), controller.getFromUI(false));
		clickOn("#cbAntiAliasing");
		assertEquals("arguments should match", defaultBuilder.antiAliasing(true).build(), controller.getFromUI(false));
		clickOn("#cbAntiAliasing");
		assertEquals("arguments should match", defaultBuilder.antiAliasing(false).build(), controller.getFromUI(false));
		clickOn("#cbAndroidIncludeLdpiTvdpi");
		assertEquals("arguments should match", defaultBuilder.includeAndroidLdpiTvdpi(true).build(), controller.getFromUI(false));
		clickOn("#cbAndroidIncludeLdpiTvdpi");
		assertEquals("arguments should match", defaultBuilder.includeAndroidLdpiTvdpi(false).build(), controller.getFromUI(false));
		clickOn("#cbHaltOnError");
		assertEquals("arguments should match", defaultBuilder.haltOnError(true).build(), controller.getFromUI(false));
		clickOn("#cbHaltOnError");
		assertEquals("arguments should match", defaultBuilder.haltOnError(false).build(), controller.getFromUI(false));
		clickOn("#cbDryRun");
		assertEquals("arguments should match", defaultBuilder.dryRun(true).build(), controller.getFromUI(false));
		clickOn("#cbDryRun");
		assertEquals("arguments should match", defaultBuilder.dryRun(false).build(), controller.getFromUI(false));
		clickOn("#cbSkipUpscaling");
		assertEquals("arguments should match", defaultBuilder.skipUpscaling(true).build(), controller.getFromUI(false));
		clickOn("#cbSkipUpscaling");
		assertEquals("arguments should match", defaultBuilder.skipUpscaling(false).build(), controller.getFromUI(false));
		clickOn("#cbSkipExisting");
		assertEquals("arguments should match", defaultBuilder.skipExistingFiles(true).build(), controller.getFromUI(false));
		clickOn("#cbSkipExisting");
		assertEquals("arguments should match", defaultBuilder.skipExistingFiles(false).build(), controller.getFromUI(false));
	}

	@Test
	public void testPlatforms() throws Exception {
		for (EPlatform ePlatform : EPlatform.values()) {
			clickOn("#choicePlatform").clickOn(ePlatform.toString());
			assertEquals("arguments should match", defaultBuilder.platform(ePlatform).build(), controller.getFromUI(false));
		}
	}

	@Test
	public void testCompressions() throws Exception {
		for (EOutputCompressionMode eOutputCompressionMode : EOutputCompressionMode.values()) {
			clickOn("#choiceCompression").clickOn(eOutputCompressionMode.toString());
			assertEquals("arguments should match", defaultBuilder.compression(eOutputCompressionMode).build(), controller.getFromUI(false));
		}
	}

	@Test
	public void testRounding() throws Exception {
		for (RoundingHandler.Strategy strategy : RoundingHandler.Strategy.values()) {
			clickOn("#choiceRounding").clickOn(strategy.toString());
			assertEquals("arguments should match", defaultBuilder.scaleRoundingStragy(strategy).build(), controller.getFromUI(false));
		}
	}

	@Test
	public void testThreads() throws Exception {
		for (int i = 1; i < Arguments.MAX_THREAD_COUNT + 1; i++) {
			clickOn("#choiceThreads").clickOn(String.valueOf(i));
			assertEquals("arguments should match", defaultBuilder.threadCount(i).build(), controller.getFromUI(false));
		}
	}

	@Test
	public void testJpegQuality() throws Exception {
		clickOn("#choiceCompression").clickOn(EOutputCompressionMode.AS_JPG.toString());
		for (float i = 0f; i < 1.1f; i += 0.1) {
			String quality = String.format(Locale.US, "%.1f", i);
			clickOn("#choiceCompressionQuality").clickOn(quality);
			assertEquals("arguments should match", defaultBuilder.compression(EOutputCompressionMode.AS_JPG, Float.parseFloat(quality)).build(), controller.getFromUI(false));
		}
		clickOn("#choiceCompressionQuality").clickOn(String.format(Locale.US, "%.1f", Arguments.DEFAULT_COMPRESSION_QUALITY));
		clickOn("#choiceCompression").clickOn(EOutputCompressionMode.SAME_AS_INPUT_PREF_PNG.toString());
		assertEquals("arguments should match", defaultBuilder.compression(EOutputCompressionMode.SAME_AS_INPUT_PREF_PNG, Arguments.DEFAULT_COMPRESSION_QUALITY).build(), controller.getFromUI(false));
	}

	@Test
	public void testClickSelectFolder() throws Exception {
		clickOn("#btnSrcFolder").sleep(400).press(KeyCode.ESCAPE);
	}

	@Test
	public void testClickSelectFile() throws Exception {
		clickOn("#btnSrcFile").sleep(400).press(KeyCode.ESCAPE);
	}

	@Test
	public void testClickSelectDstFolder() throws Exception {
		clickOn("#btnDstFolder").sleep(400).press(KeyCode.ESCAPE);
	}

	@Test
	public void testVerbose() throws Exception {
		clickOn("#cbVerboseLog");
		assertEquals("arguments should match", defaultBuilder.verboseLog(false).build(), controller.getFromUI(false));
		clickOn("#cbVerboseLog");
		assertEquals("arguments should match", defaultBuilder.verboseLog(true).build(), controller.getFromUI(false));
		clickOn("#textFieldConsole").write("a console output");
	}

	@Test
	public void testCase1() throws Exception {
		clickOn("#rbDpHeight").clickOn("#textFieldDp").write("64");
		clickOn("#choicePlatform").clickOn(EPlatform.ANDROID.toString());
		clickOn("#cbSkipUpscaling");
		clickOn("#chEnablePngCrush");
		clickOn("#choiceThreads").clickOn(String.valueOf(2));
		assertEquals("arguments should match", new Arguments.Builder(defaultSrcFolder, 64).verboseLog(true)
				.scaleType(EScaleType.DP_HEIGHT).platform(EPlatform.ANDROID).skipUpscaling(true).threadCount(2).enablePngCrush(true)
				.build(), controller.getFromUI(false));
	}

	@Test
	public void testCase2() throws Exception {
		clickOn("#rbDpWidth").clickOn("#textFieldDp").write("128");
		clickOn("#choicePlatform").clickOn(EPlatform.ANDROID.toString());
		clickOn("#choiceCompression").clickOn(EOutputCompressionMode.AS_JPG.toString());
		String quality = String.format(Locale.US, "%.1f", 0.3);
		clickOn("#choiceCompressionQuality").clickOn(quality);
		clickOn("#cbSkipExisting");
		assertEquals("arguments should match", new Arguments.Builder(defaultSrcFolder, 128).verboseLog(true)
				.scaleType(EScaleType.DP_WIDTH).platform(EPlatform.ANDROID).skipExistingFiles(true)
				.compression(EOutputCompressionMode.AS_JPG, Float.parseFloat(quality))
				.build(), controller.getFromUI(false));
	}

}
