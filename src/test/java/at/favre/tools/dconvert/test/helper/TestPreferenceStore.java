package at.favre.tools.dconvert.test.helper;

import at.favre.tools.dconvert.arg.Arguments;
import at.favre.tools.dconvert.ui.IPreferenceStore;
import org.junit.Ignore;

/**
 * For UI tests
 */
@Ignore("not a test class")
public class TestPreferenceStore implements IPreferenceStore {
    private Arguments arg;

    @Override
    public void save(Arguments arg) {
        this.arg = arg;
    }

    @Override
    public Arguments get() {
        return arg;
    }
}
