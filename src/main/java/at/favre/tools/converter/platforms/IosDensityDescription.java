package at.favre.tools.converter.platforms;

/**
 * Needed info to convert for iOS
 */
public class IOSDensityDescription extends DensityDescription {
	public final String postFix;

	protected IOSDensityDescription(float scale, String name, String postFix) {
		super(scale, name);
		this.postFix = postFix;
	}
}
