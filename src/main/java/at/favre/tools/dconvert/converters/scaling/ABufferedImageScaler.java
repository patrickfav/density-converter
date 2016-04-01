package at.favre.tools.dconvert.converters.scaling;


public abstract class ABufferedImageScaler implements IBufferedImageScaler {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ABufferedImageScaler that = (ABufferedImageScaler) o;

        return getClass().getName() != null ? getClass().getName().equals(that.getClass().getName()) : that.getClass().getName() == null;

    }

    @Override
    public int hashCode() {
        return getClass().getName() != null ? getClass().getName().hashCode() : 0;
    }
}
