package at.favre.tools.dconvert.converters;

import java.io.File;
import java.util.List;

/**
 * Wrapper for a result
 */
public class Result {
	public final Exception exception;
	public final List<File> processedFiles;
	public final String log;

	public Result(String log, Exception exception, List<File> processedFiles) {
		this.log = log;
		this.exception = exception;
		this.processedFiles = processedFiles;
	}

	public Result(String log, List<File> processedFiles) {
		this(log, null, processedFiles);
	}
}
