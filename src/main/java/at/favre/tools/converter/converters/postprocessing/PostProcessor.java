package at.favre.tools.converter.converters.postprocessing;

import java.io.File;

/**
 * Created by PatrickF on 28.02.2016.
 */
public interface PostProcessor {
	String process(File rawFile);
}
