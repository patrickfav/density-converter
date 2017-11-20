package at.favre.tools.dconvert.test;

import at.favre.tools.dconvert.Main;
import at.favre.tools.dconvert.arg.Arguments;
import at.favre.tools.dconvert.ui.CLIInterpreter;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * System test through command line interface
 */
public class CLISystemTest extends AIntegrationTest {
    private String defaultArgRaw;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        defaultArgRaw = "-src \"" + src.getAbsolutePath() + "\" -dst \"" + dst.getAbsolutePath() + "\" -scale 4";

    }

    @Test
    public void testZeroFilesInput() throws Exception {
        Arguments arg = CLIInterpreter.parse(CLIParserTest.asArgArray(defaultArgRaw));
        Main.main(CLIParserTest.asArgArray(defaultArgRaw));
        AConverterTest.checkMultiPlatformConvert(dst, arg, Collections.emptyList());
    }

    @Test
    public void testSingleFileIosPlatformConverter() throws Exception {
        defaultArgRaw += " -" + CLIInterpreter.PLATFORM_ARG + " ios";
        List<File> files = AConverterTest.copyToTestPath(src, "png_example1_alpha_144.png");
        Arguments arg = CLIInterpreter.parse(CLIParserTest.asArgArray(defaultArgRaw));
        Main.main(CLIParserTest.asArgArray(defaultArgRaw));
        IOSConverterTest.checkOutDirIos(dst, arg, files);
    }

    @Test
    public void testAndroidPlatformConverter() throws Exception {
        List<File> files = AConverterTest.copyToTestPath(src, "png_example3_alpha_128.png", "png_example1_alpha_144.png", "jpg_example2_512.jpg", "gif_example_640.gif", "png_example4_500.png", "psd_example_827.psd");
        defaultArgRaw += " -" + CLIInterpreter.PLATFORM_ARG + " android";
        Arguments arg = CLIInterpreter.parse(CLIParserTest.asArgArray(defaultArgRaw));
        Main.main(CLIParserTest.asArgArray(defaultArgRaw));
        AndroidConverterTest.checkOutDirAndroid(dst, arg, files);
    }

    @Test
    public void testAllPlatformConverter() throws Exception {
        List<File> files = AConverterTest.copyToTestPath(src, "png_example3_alpha_128.png", "png_example1_alpha_144.png", "jpg_example2_512.jpg");
        defaultArgRaw += " -" + CLIInterpreter.PLATFORM_ARG + " all";
        Arguments arg = CLIInterpreter.parse(CLIParserTest.asArgArray(defaultArgRaw));
        Main.main(CLIParserTest.asArgArray(defaultArgRaw));
        AConverterTest.checkMultiPlatformConvert(dst, arg, files);
    }
}
