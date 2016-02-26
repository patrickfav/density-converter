package at.favre.tools.converter.platforms;

/**
 * Callback to inform the callee of the result
 */
public interface ConverterCallback {

	void success(String log);

	void failure(Exception e);

}
