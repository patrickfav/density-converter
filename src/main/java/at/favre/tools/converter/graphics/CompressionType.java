package at.favre.tools.converter.graphics;

/**
 * Created by PatrickF on 25.02.2016.
 */
public enum CompressionType {

	GIF("gif"), PNG("png"), JPEG("jpg");

	public final String formatName;

	CompressionType(String formatName) {
		this.formatName = formatName;
	}
}
