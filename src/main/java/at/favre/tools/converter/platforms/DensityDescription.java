package at.favre.tools.converter.platforms;

/**
 * Base class for information on creating different densities for the platforms
 */
public class DensityDescription implements Comparable<DensityDescription> {
	public final float scale;
	public final String name;

	protected DensityDescription(float scale, String name) {
		this.scale = scale;
		this.name = name;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		DensityDescription that = (DensityDescription) o;

		if (Float.compare(that.scale, scale) != 0) return false;
		return !(name != null ? !name.equals(that.name) : that.name != null);

	}

	@Override
	public int hashCode() {
		int result = (scale != +0.0f ? Float.floatToIntBits(scale) : 0);
		result = 31 * result + (name != null ? name.hashCode() : 0);
		return result;
	}

	@Override
	public int compareTo(DensityDescription o) {
		return Float.compare(scale, o.scale);
	}
}
