package at.favre.tools.dconvert.test;

import at.favre.tools.dconvert.arg.*;
import at.favre.tools.dconvert.test.helper.TestPreferenceStore;
import at.favre.tools.dconvert.ui.GUI;
import at.favre.tools.dconvert.ui.GUIController;
import javafx.stage.Stage;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.testfx.framework.junit.ApplicationTest;

import java.io.File;

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
		assertEquals("default should match", defaultBuilder.build(), controller.getFromUI());
	}

	@After
	public void tearDown() throws Exception {
		clickOn("#btnReset");
//		assertEquals("should be default after reset", defaultBuilder.build(),controller.getFromUI());
	}

	@Test
	public void testScaleTypes() throws Exception {
		clickOn("#rbDpWidth").clickOn("#textFieldDp").write("48");
		assertEquals("arguments should match",
				new Arguments.Builder(defaultSrcFolder, 48).scaleType(EScaleType.DP_WIDTH).verboseLog(true).build(), controller.getFromUI());
		clickOn("#rbDpHeight").clickOn("#textFieldDp").write("1");
		assertEquals("arguments should match",
				new Arguments.Builder(defaultSrcFolder, 148).scaleType(EScaleType.DP_HEIGHT).verboseLog(true).build(), controller.getFromUI());
		clickOn("#rbFactor").clickOn("#scaleSlider");
		assertEquals("arguments should match",
				new Arguments.Builder(defaultSrcFolder, 3).scaleType(EScaleType.FACTOR).verboseLog(true).build(), controller.getFromUI());
	}

	@Test
	public void testPostProcessors() throws Exception {
		clickOn("#cbPostConvertWebp");
		assertEquals("arguments should match", defaultBuilder.postConvertWebp(true).build(), controller.getFromUI());
		clickOn("#cbPostConvertWebp");
		assertEquals("arguments should match", defaultBuilder.postConvertWebp(false).build(), controller.getFromUI());
		clickOn("#chEnablePngCrush");
		assertEquals("arguments should match", defaultBuilder.enablePngCrush(true).build(), controller.getFromUI());
		clickOn("#chEnablePngCrush");
		assertEquals("arguments should match", defaultBuilder.enablePngCrush(false).build(), controller.getFromUI());
	}

	@Test
	public void testCBOptions() throws Exception {
		clickOn("#cbMipmapInsteadDrawable");
		assertEquals("arguments should match", defaultBuilder.createMipMapInsteadOfDrawableDir(true).build(), controller.getFromUI());
		clickOn("#cbMipmapInsteadDrawable");
		assertEquals("arguments should match", defaultBuilder.createMipMapInsteadOfDrawableDir(false).build(), controller.getFromUI());
		clickOn("#cbAntiAliasing");
		assertEquals("arguments should match", defaultBuilder.antiAliasing(true).build(), controller.getFromUI());
		clickOn("#cbAntiAliasing");
		assertEquals("arguments should match", defaultBuilder.antiAliasing(false).build(), controller.getFromUI());
		clickOn("#cbAndroidIncludeLdpiTvdpi");
		assertEquals("arguments should match", defaultBuilder.includeAndroidLdpiTvdpi(true).build(), controller.getFromUI());
		clickOn("#cbAndroidIncludeLdpiTvdpi");
		assertEquals("arguments should match", defaultBuilder.includeAndroidLdpiTvdpi(false).build(), controller.getFromUI());
		clickOn("#cbHaltOnError");
		assertEquals("arguments should match", defaultBuilder.haltOnError(true).build(), controller.getFromUI());
		clickOn("#cbHaltOnError");
		assertEquals("arguments should match", defaultBuilder.haltOnError(false).build(), controller.getFromUI());
		clickOn("#cbDryRun");
		assertEquals("arguments should match", defaultBuilder.dryRun(true).build(), controller.getFromUI());
		clickOn("#cbDryRun");
		assertEquals("arguments should match", defaultBuilder.dryRun(false).build(), controller.getFromUI());
		clickOn("#cbSkipUpscaling");
		assertEquals("arguments should match", defaultBuilder.skipUpscaling(true).build(), controller.getFromUI());
		clickOn("#cbSkipUpscaling");
		assertEquals("arguments should match", defaultBuilder.skipUpscaling(false).build(), controller.getFromUI());
		clickOn("#cbSkipExisting");
		assertEquals("arguments should match", defaultBuilder.skipExistingFiles(true).build(), controller.getFromUI());
		clickOn("#cbSkipExisting");
		assertEquals("arguments should match", defaultBuilder.skipExistingFiles(false).build(), controller.getFromUI());
	}

	@Test
	public void testPlatforms() throws Exception {
		for (EPlatform ePlatform : EPlatform.values()) {
			clickOn("#choicePlatform").clickOn(ePlatform.toString());
			assertEquals("arguments should match", defaultBuilder.platform(ePlatform).build(), controller.getFromUI());
		}
	}

	@Test
	public void testCompressions() throws Exception {
		for (EOutputCompressionMode eOutputCompressionMode : EOutputCompressionMode.values()) {
			clickOn("#choiceCompression").clickOn(eOutputCompressionMode.toString());
			assertEquals("arguments should match", defaultBuilder.compression(eOutputCompressionMode).build(), controller.getFromUI());
		}
	}

	@Test
	public void testRounding() throws Exception {
		for (RoundingHandler.Strategy strategy : RoundingHandler.Strategy.values()) {
			clickOn("#choiceRounding").clickOn(strategy.toString());
			assertEquals("arguments should match", defaultBuilder.scaleRoundingStragy(strategy).build(), controller.getFromUI());
		}
	}

	@Test
	public void testThreads() throws Exception {
		for (int i = 0; i < Arguments.DEFAULT_THREAD_COUNT; i++) {

		}
		for (RoundingHandler.Strategy strategy : RoundingHandler.Strategy.values()) {
			clickOn("#choiceRounding").clickOn(strategy.toString());
			assertEquals("arguments should match", defaultBuilder.scaleRoundingStragy(strategy).build(), controller.getFromUI());
		}
	}
}
