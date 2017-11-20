package at.favre.tools.dconvert.test.helper;

import at.favre.tools.dconvert.converters.Result;
import at.favre.tools.dconvert.converters.postprocessing.IPostProcessor;
import org.junit.Ignore;

import java.io.File;
import java.util.Collections;

/**
 * For testing worker handlers
 */
@Ignore
public class MockProcessor implements IPostProcessor {
    private long sleep = 66;
    private Exception exception = null;

    public MockProcessor() {
    }

    public MockProcessor(long sleep) {
        this.sleep = sleep;
    }

    public MockProcessor(Exception exception) {
        this.exception = exception;
    }

    @Override
    public Result process(File rawFile, boolean keepOriginal) {
        try {
            Thread.sleep(sleep);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return new Result("test done", exception, Collections.singletonList(rawFile));
    }

    @Override
    public boolean isSupported() {
        return true;
    }
}
