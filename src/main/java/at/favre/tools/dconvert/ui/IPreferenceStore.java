package at.favre.tools.dconvert.ui;

import at.favre.tools.dconvert.arg.Arguments;

/**
 * Interface for preference store in the ui
 */
public interface IPreferenceStore {
	/**
	 * Persistently saves the given argument
	 *
	 * @param arg
	 */
	void save(Arguments arg);

	/**
	 * Gets the arguemnt object from the persistence store
	 *
	 * @return the arg or null if not set or could not be read
	 */
	Arguments get();
}
