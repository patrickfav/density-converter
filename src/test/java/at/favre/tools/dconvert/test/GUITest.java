package at.favre.tools.dconvert.test;

import at.favre.tools.dconvert.arg.Arguments;
import at.favre.tools.dconvert.arg.EOutputCompressionMode;
import at.favre.tools.dconvert.arg.EPlatform;
import at.favre.tools.dconvert.arg.EScaleMode;
import at.favre.tools.dconvert.arg.EScalingAlgorithm;
import at.favre.tools.dconvert.arg.RoundingHandler;
import at.favre.tools.dconvert.converters.postprocessing.MozJpegProcessor;
import at.favre.tools.dconvert.converters.postprocessing.PngCrushProcessor;
import at.favre.tools.dconvert.converters.postprocessing.WebpProcessor;
import at.favre.tools.dconvert.test.helper.TestPreferenceStore;
import at.favre.tools.dconvert.ui.GUI;
import at.favre.tools.dconvert.ui.GUIController;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.testfx.framework.junit.ApplicationTest;

import java.awt.*;
import java.io.File;
import java.util.Collections;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

/**
 * Tests GUI
 */
public class GUITest extends ApplicationTest {
    private static final boolean HEADLESS = true;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();
    private GUIController controller;
    private Arguments.Builder defaultBuilder;
    private File defaultSrcFolder;
    private ResourceBundle bundle = ResourceBundle.getBundle("bundles.strings", Locale.getDefault());
    private Scene scene;

    @BeforeClass
    public static void setupSpec() {
        if (HEADLESS) {
            System.setProperty("testfx.robot", "glass");
            System.setProperty("testfx.headless", "true");
            System.setProperty("prism.order", "sw");
            System.setProperty("prism.text", "t2k");
            System.setProperty("java.awt.headless", "true");
            System.setProperty("headless.geometry", "1920x1200-32");
        }
    }

    @Override
    public void start(Stage stage) throws Exception {
        controller = GUI.setup(stage, new TestPreferenceStore(), new Dimension(1920, 1080));
        stage.show();
        scene = stage.getScene();
    }

    @Before
    public void setUp() throws Exception {
        defaultSrcFolder = temporaryFolder.newFolder();
        defaultBuilder = new Arguments.Builder(defaultSrcFolder, Arguments.DEFAULT_SCALE).guiAdvancedOptions(true).verboseLog(true);
        controller.setSrcForTest(defaultSrcFolder);
        clickOn("#rbOptAdvanced");
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
                new Arguments.Builder(defaultSrcFolder, 48).scaleMode(EScaleMode.DP_WIDTH).guiAdvancedOptions(true).verboseLog(true).build(), controller.getFromUI(false));
        clickOn("#rbDpHeight").clickOn("#textFieldDp").write("1");
        assertEquals("arguments should match",
                new Arguments.Builder(defaultSrcFolder, 148).scaleMode(EScaleMode.DP_HEIGHT).guiAdvancedOptions(true).verboseLog(true).build(), controller.getFromUI(false));
        clickOn("#rbFactor").clickOn("#scaleSlider");
        assertEquals("arguments should match",
                new Arguments.Builder(defaultSrcFolder, 3).scaleMode(EScaleMode.FACTOR).guiAdvancedOptions(true).verboseLog(true).build(), controller.getFromUI(false));
    }

    @Test
    public void testPostProcessors() throws Exception {
        if (new WebpProcessor().isSupported()) {
            clickOn("#cbPostConvertWebp");
            assertEquals("arguments should match", defaultBuilder.postConvertWebp(true).build(), controller.getFromUI(false));
            clickOn("#cbPostConvertWebp");
            assertEquals("arguments should match", defaultBuilder.postConvertWebp(false).build(), controller.getFromUI(false));
        }
        if (new PngCrushProcessor().isSupported()) {
            clickOn("#cbEnablePngCrush");
            assertEquals("arguments should match", defaultBuilder.enablePngCrush(true).build(), controller.getFromUI(false));
            clickOn("#cbEnablePngCrush");
            assertEquals("arguments should match", defaultBuilder.enablePngCrush(false).build(), controller.getFromUI(false));
        }
        if (new MozJpegProcessor().isSupported()) {
            clickOn("#cbEnableMozJpeg");
            assertEquals("arguments should match", defaultBuilder.enableMozJpeg(true).build(), controller.getFromUI(false));
            clickOn("#cbEnableMozJpeg");
            assertEquals("arguments should match", defaultBuilder.enableMozJpeg(false).build(), controller.getFromUI(false));
        }

        clickOn("#cbKeepUnoptimized");
        assertEquals("arguments should match", defaultBuilder.keepUnoptimizedFilesPostProcessor(true).build(), controller.getFromUI(false));
        clickOn("#cbKeepUnoptimized");
        assertEquals("arguments should match", defaultBuilder.keepUnoptimizedFilesPostProcessor(false).build(), controller.getFromUI(false));
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
        clickOn("#cbIosCreateImageset");
        assertEquals("arguments should match", defaultBuilder.iosCreateImagesetFolders(true).build(), controller.getFromUI(false));
        clickOn("#cbIosCreateImageset");
        assertEquals("arguments should match", defaultBuilder.iosCreateImagesetFolders(false).build(), controller.getFromUI(false));
        clickOn("#cbCleanBeforeConvert");
        assertEquals("arguments should match", defaultBuilder.clearDirBeforeConvert(true).build(), controller.getFromUI(false));
        clickOn("#cbCleanBeforeConvert");
        assertEquals("arguments should match", defaultBuilder.clearDirBeforeConvert(false).build(), controller.getFromUI(false));
    }

    @Test
    public void testPlatforms() throws Exception {
        for (EPlatform ePlatform : Arguments.DEFAULT_PLATFORM) {
            clickOn(getIdForPlatform(ePlatform));
        }

        for (EPlatform ePlatform : EPlatform.values()) {
            clickOn(getIdForPlatform(ePlatform));
            assertEquals("arguments should match", defaultBuilder.platform(Collections.singleton(ePlatform)).build(), controller.getFromUI(false));
            clickOn(getIdForPlatform(ePlatform));
        }
    }

    //this test needs fixing: bug is that click on algo name is not distinct - sometimes it picks the correct choicebox, sometimes the other (up <> downscaling have the same values
    @Ignore
    public void testDownScalingQuality() throws Exception {
        Assume.assumeFalse("this only seems to work in non-headless test", HEADLESS);
        for (EScalingAlgorithm algo : EScalingAlgorithm.getAllEnabled()) {
            if (algo.getSupportedForType().contains(EScalingAlgorithm.Type.DOWNSCALING)) {
                clickOn("#choiceDownScale").clickOn(algo.toString());
                assertEquals("arguments should match", defaultBuilder.downScaleAlgorithm(algo).build(), controller.getFromUI(false));
            }
        }
    }

    //this test needs fixing: bug is that click on algo name is not distinct - sometimes it picks the correct choicebox, sometimes the other (up <> downscaling have the same values
    @Ignore
    public void testUpScalingQuality() throws Exception {
        for (EScalingAlgorithm algo : EScalingAlgorithm.getAllEnabled()) {
            if (algo.getSupportedForType().contains(EScalingAlgorithm.Type.UPSCALING)) {

                ChoiceBox choiceBox = (ChoiceBox) scene.lookup("#choiceUpScale");
                //choiceBox.getSelectionModel().
                for (Object o : choiceBox.getItems()) {
                    if (o.toString().equals(algo.toString())) {

                    }
                }
                clickOn("#choiceUpScale").clickOn(algo.toString());
                assertEquals("arguments should match", defaultBuilder.upScaleAlgorithm(algo).build(), controller.getFromUI(false));
            }
        }
    }

    private String getIdForPlatform(EPlatform platform) {
        switch (platform) {
            case ANDROID:
                return "#tgAndroid";
            case IOS:
                return "#tgIos";
            case WINDOWS:
                return "#tgWindows";
            case WEB:
                return "#tgWeb";
            default:
                throw new IllegalArgumentException("platform unknown");
        }
    }

    @Test
    public void testCompressions() throws Exception {
        for (EOutputCompressionMode eOutputCompressionMode : EOutputCompressionMode.values()) {
            clickOn("#choiceCompression").clickOn(bundle.getString(eOutputCompressionMode.rbKey));
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
    @Ignore
    public void testThreads() throws Exception {
        for (int i = 1; i < Arguments.MAX_THREAD_COUNT + 1; i++) {
            sleep(18, TimeUnit.MILLISECONDS); //hack because after testfx update headless mode failed
            clickOn("#choiceThreads").clickOn(String.valueOf(i));
            assertEquals("arguments should match", defaultBuilder.threadCount(i).build(), controller.getFromUI(false));
        }
    }

    @Test
    public void testJpegQuality() throws Exception {
        clickOn("#choiceCompression").clickOn(bundle.getString(EOutputCompressionMode.AS_JPG.rbKey));
        for (float i = 0f; i < 1.1f; i += 0.1) {
            clickOn("#choiceCompressionQuality").clickOn(GUIController.toJpgQ(i));
            assertEquals("arguments should match", defaultBuilder.compression(EOutputCompressionMode.AS_JPG, Float.parseFloat(String.format(Locale.US, "%.1f", i))).build(), controller.getFromUI(false));
        }
        clickOn("#choiceCompressionQuality").clickOn(GUIController.toJpgQ(Arguments.DEFAULT_COMPRESSION_QUALITY));
        clickOn("#choiceCompression").clickOn(bundle.getString(EOutputCompressionMode.SAME_AS_INPUT_PREF_PNG.rbKey));
        assertEquals("arguments should match", defaultBuilder.compression(EOutputCompressionMode.SAME_AS_INPUT_PREF_PNG, Arguments.DEFAULT_COMPRESSION_QUALITY).build(), controller.getFromUI(false));
    }

    @Test
    public void testClickSelectFolder() {
        if (!HEADLESS) {
            clickOn("#btnSrcFolder").sleep(400).press(KeyCode.ESCAPE);
        }
    }

    @Test
    public void testClickSelectFile() {
        if (!HEADLESS) {
            clickOn("#btnSrcFile").sleep(400).press(KeyCode.ESCAPE);
        }
    }

    @Test
    public void testClickSelectDstFolder() {
        if (!HEADLESS) {
            clickOn("#btnDstFolder").sleep(400).press(KeyCode.ESCAPE);
        }
    }

    @Test
    public void testCase1() throws Exception {
        for (EPlatform ePlatform : Arguments.DEFAULT_PLATFORM) {
            clickOn(getIdForPlatform(ePlatform));
        }

        clickOn("#rbDpHeight").clickOn("#textFieldDp").write("64");
        clickOn(getIdForPlatform(EPlatform.ANDROID));
        clickOn("#cbSkipUpscaling");
        clickOn("#cbSkipExisting");

        assertEquals("arguments should match", new Arguments.Builder(defaultSrcFolder, 64).guiAdvancedOptions(true).verboseLog(true)
                .scaleMode(EScaleMode.DP_HEIGHT).platform(Collections.singleton(EPlatform.ANDROID)).skipUpscaling(true).threadCount(4).skipExistingFiles(true)
                .build(), controller.getFromUI(false));
    }

    @Test
    public void testCase2() throws Exception {
        for (EPlatform ePlatform : Arguments.DEFAULT_PLATFORM) {
            clickOn(getIdForPlatform(ePlatform));
        }

        clickOn("#rbDpWidth").clickOn("#textFieldDp").write("128");
        clickOn(getIdForPlatform(EPlatform.ANDROID));
        clickOn("#choiceCompression").clickOn(bundle.getString(EOutputCompressionMode.AS_JPG.rbKey));
        float jpegQ = 0.3f;
        clickOn("#choiceCompressionQuality").clickOn(GUIController.toJpgQ(jpegQ));
        clickOn("#cbSkipExisting");
        assertEquals("arguments should match", new Arguments.Builder(defaultSrcFolder, 128).guiAdvancedOptions(true).verboseLog(true)
                .scaleMode(EScaleMode.DP_WIDTH).platform(Collections.singleton(EPlatform.ANDROID)).skipExistingFiles(true)
                .compression(EOutputCompressionMode.AS_JPG, Float.parseFloat(String.format(Locale.US, "%.1f", jpegQ)))
                .build(), controller.getFromUI(false));
    }

}
