package at.favre.tools.dconvert.test;

import at.favre.tools.dconvert.WorkerHandler;
import at.favre.tools.dconvert.arg.Arguments;
import at.favre.tools.dconvert.arg.EPlatform;
import at.favre.tools.dconvert.converters.AndroidConverter;
import at.favre.tools.dconvert.converters.IPlatformConverter;
import at.favre.tools.dconvert.converters.postprocessing.IPostProcessor;
import at.favre.tools.dconvert.test.helper.MockException;
import at.favre.tools.dconvert.test.helper.MockProcessor;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test for worker handler
 */
public class WorkerHandlerTest extends AIntegrationTest {

    private Random random;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        random = new Random(12363012L);
    }

    @Test
    public void testZeroFilesInput() throws Exception {
        TestCallback callback = new TestCallback(0, Collections.emptyList(), false, latch);
        new WorkerHandler<>(Collections.singletonList(new MockProcessor()), arguments, callback).start(Collections.emptyList());
        assertTrue(latch.await(WAIT_SEC, TimeUnit.SECONDS));
        checkResult(callback);
    }

    @Test
    public void test66FilesInput() throws Exception {
        List<File> files = createFiles(66);
        TestCallback callback = new TestCallback(files.size(), Collections.emptyList(), false, latch);
        new WorkerHandler<>(Collections.singletonList(new MockProcessor()), arguments, callback).start(files);
        assertTrue(latch.await(WAIT_SEC, TimeUnit.SECONDS));
        checkResult(callback);
    }

    @Test
    public void test33With3ProcessorsFilesInput() throws Exception {
        List<File> files = createFiles(33);
        List<IPostProcessor> postProcessors = createProcessors(3);
        TestCallback callback = new TestCallback(files.size() * postProcessors.size(), Collections.emptyList(), false, latch);
        new WorkerHandler<>(postProcessors, arguments, callback).start(files);
        assertTrue(latch.await(WAIT_SEC, TimeUnit.SECONDS));
        checkResult(callback);
    }

    @Test
    public void test5With33ProcessorsFilesInput() throws Exception {
        List<File> files = createFiles(5);
        List<IPostProcessor> postProcessors = createProcessors(33);
        TestCallback callback = new TestCallback(files.size() * postProcessors.size(), Collections.emptyList(), false, latch);
        new WorkerHandler<>(postProcessors, arguments, callback).start(files);
        assertTrue(latch.await(WAIT_SEC, TimeUnit.SECONDS));
        checkResult(callback);
    }

    @Test
    public void testShouldHaveException() throws Exception {
        List<File> files = createFiles(1);
        Exception exception = new MockException();
        TestCallback callback = new TestCallback(files.size(), Collections.singletonList(exception), false, latch);
        new WorkerHandler<>(Collections.singletonList(new MockProcessor(exception)), arguments, callback).start(files);
        assertTrue(latch.await(WAIT_SEC, TimeUnit.SECONDS));
        checkResult(callback);
    }

    @Test
    public void testShouldHave5Exception() throws Exception {
        List<Exception> exception = Arrays.asList(new MockException(), new MockException(), new MockException(), new MockException(), new MockException());
        List<File> files = createFiles(exception.size());
        TestCallback callback = new TestCallback(files.size(), exception, false, latch);
        new WorkerHandler<>(Collections.singletonList(new MockProcessor(new MockException())), arguments, callback).start(files);
        assertTrue(latch.await(WAIT_SEC, TimeUnit.SECONDS));
        checkResult(callback);
    }

    @Test
    public void testShouldHaltOnException() throws Exception {
        List<Exception> exception = Arrays.asList(new MockException());
        List<File> files = createFiles(exception.size());
        TestCallback callback = new TestCallback(files.size(), exception, true, latch);
        new WorkerHandler<>(Collections.singletonList(new MockProcessor(new MockException())),
                new Arguments.Builder(null, Arguments.DEFAULT_SCALE).threadCount(4).haltOnError(true).skipParamValidation(true).build(), callback).start(files);
        assertTrue(latch.await(WAIT_SEC, TimeUnit.SECONDS));
        checkResult(callback);
    }

    @Test
    public void testAndroidConverterInHandler() throws Exception {
        List<File> files = AConverterTest.copyToTestPath(src, "png_example2_alpha_144.png", "gif_example_640.gif", "jpg_example_1920.jpg");
        Arguments arg = new Arguments.Builder(src, Arguments.DEFAULT_SCALE).dstFolder(dst).platform(Collections.singleton(EPlatform.ANDROID)).threadCount(4).build();
        TestCallback callback = new TestCallback(files.size(), Collections.emptyList(), false, latch);
        new WorkerHandler<>(Collections.singletonList(new AndroidConverter()), arg, callback).start(files);
        assertTrue(latch.await(WAIT_SEC, TimeUnit.SECONDS));
        checkResult(callback);
        AndroidConverterTest.checkOutDirAndroid(dst, arg, files);
    }

    @Test
    public void testAllPlatformConverterInHandler() throws Exception {
        List<File> files = AConverterTest.copyToTestPath(src, "png_example3_alpha_128.png", "png_example1_alpha_144.png", "jpg_example2_512.jpg");
        List<IPlatformConverter> converters = EPlatform.getAllConverters();
        Arguments arg = new Arguments.Builder(src, Arguments.DEFAULT_SCALE).platform(EPlatform.getAll()).dstFolder(dst).threadCount(4).build();
        TestCallback callback = new TestCallback(files.size() * converters.size(), Collections.emptyList(), false, latch);
        new WorkerHandler<>(converters, arg, callback).start(files);
        assertTrue(latch.await(WAIT_SEC, TimeUnit.SECONDS));
        checkResult(callback);
        AConverterTest.checkMultiPlatformConvert(dst, arg, files);
    }

    private void checkResult(TestCallback callback) {
        assertEquals(callback.expectedJobs, callback.actualJobs);
        assertEquals(callback.expectedExceptions, callback.actualExceptions);
        assertEquals(callback.expectedHaltDuringProcess, callback.actualHaltDuringProcess);
    }

    private List<File> createFiles(int count) {
        List<File> files = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            files.add(new File("mock" + i));
        }
        return files;
    }

    private List<IPostProcessor> createProcessors(int count) {
        List<IPostProcessor> processors = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            processors.add(new MockProcessor(22 + random.nextInt(50)));
        }
        return processors;
    }

    private static class TestCallback implements WorkerHandler.Callback {
        private final int expectedJobs;
        private final List<Exception> expectedExceptions;
        private final boolean expectedHaltDuringProcess;
        private final CountDownLatch latch;
        private int actualJobs;
        private List<Exception> actualExceptions;
        private boolean actualHaltDuringProcess;

        public TestCallback(int expectedJobs, List<Exception> expectedExceptions, boolean expectedHaltDuringProcess, CountDownLatch latch) {
            this.expectedJobs = expectedJobs;
            this.expectedExceptions = expectedExceptions;
            this.expectedHaltDuringProcess = expectedHaltDuringProcess;
            this.latch = latch;
        }

        @Override
        public void onProgress(float percent) {
        }

        @Override
        public void onFinished(int finishedJobs, List<File> outFiles, StringBuilder log, List<Exception> exceptions, boolean haltedDuringProcess) {
            actualJobs = finishedJobs;
            actualExceptions = exceptions;
            actualHaltDuringProcess = haltedDuringProcess;
            latch.countDown();
        }
    }
}
