package at.favre.tools.dconvert.test.helper;

/**
 * Exception to throw in test
 */
public class MockException extends Exception {
    private final String s = getClass().getName();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MockException that = (MockException) o;

        return s != null ? s.equals(that.s) : that.s == null;

    }

    @Override
    public int hashCode() {
        return s != null ? s.hashCode() : 0;
    }
}
