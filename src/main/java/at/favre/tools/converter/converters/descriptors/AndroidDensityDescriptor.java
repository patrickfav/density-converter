package at.favre.tools.converter.converters.descriptors;

/**
 * Needed info to convert for Android
 */
public class AndroidDensityDescriptor extends DensityDescriptor {
	public final String folderName;

	public AndroidDensityDescriptor(float scale, String name, String folderName) {
		super(scale, name);
		this.folderName = folderName;
	}

	@Override
	public String toString() {
		return "AndroidDensityDescription{" +
				"folderName='" + folderName + '\'' +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;

		AndroidDensityDescriptor that = (AndroidDensityDescriptor) o;

		return !(folderName != null ? !folderName.equals(that.folderName) : that.folderName != null);

	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + (folderName != null ? folderName.hashCode() : 0);
		return result;
	}
}
