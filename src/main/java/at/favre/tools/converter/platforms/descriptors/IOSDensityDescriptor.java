package at.favre.tools.converter.platforms.descriptors;

/**
 * Needed info to convert for iOS
 */
public class IOSDensityDescriptor extends DensityDescriptor {
	public final String postFix;

	public IOSDensityDescriptor(float scale, String name, String postFix) {
		super(scale, name);
		this.postFix = postFix;
	}
}
