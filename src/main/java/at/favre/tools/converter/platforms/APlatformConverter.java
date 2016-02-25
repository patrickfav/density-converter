package at.favre.tools.converter.platforms;

import at.favre.tools.converter.RoundingHandler;

/**
 * Shared code
 */
public abstract class APlatformConverter implements IPlatformConverter {

	protected RoundingHandler roundingHandler;

	public void setup(RoundingHandler roundingHandler) {
		this.roundingHandler = roundingHandler;
	}
}
