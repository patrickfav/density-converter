package at.favre.tools.dconvert.test;

import at.favre.tools.dconvert.arg.Arguments;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.util.concurrent.CountDownLatch;

/**
 * Basics of creating integration style tests (real input, real output)
 */
public class AIntegrationTest {
    static final long WAIT_SEC = 7;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();
    public Arguments arguments;
    public CountDownLatch latch;
    public File src;
    public File dst;

    @Before
    public void setUp() throws Exception {
        arguments = new Arguments.Builder(null, Arguments.DEFAULT_SCALE).threadCount(4).skipParamValidation(true).build();
        latch = new CountDownLatch(1);
        src = temporaryFolder.newFolder("convert-test", "src");
        dst = temporaryFolder.newFolder("convert-test", "out");
    }
}
