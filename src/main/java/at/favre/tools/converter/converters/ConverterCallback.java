package at.favre.tools.converter.converters;

import java.io.File;
import java.util.List;

/**
 * Callback to inform the callee of the result
 */
public interface ConverterCallback {

	void success(String log, List<File> compressedImages);

	void failure(Exception e);

}
