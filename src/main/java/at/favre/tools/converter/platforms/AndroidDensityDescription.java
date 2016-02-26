package at.favre.tools.converter.platforms;

/**
 * Needed info to convert for Android
 */
public class AndroidDensityDescription extends DensityDescription {
	public final String folderName;

	protected AndroidDensityDescription(float scale, String name, String folderName) {
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

		AndroidDensityDescription that = (AndroidDensityDescription) o;

		return !(folderName != null ? !folderName.equals(that.folderName) : that.folderName != null);

	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + (folderName != null ? folderName.hashCode() : 0);
		return result;
	}
}
