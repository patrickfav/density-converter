package at.favre.tools.dconvert.arg;

/**
 * Supported image types
 */
public enum ImageType {

	JPG(ECompression.JPG, ECompression.JPG, new String[]{"jpeg", "jpg"}, true),
	PNG(ECompression.PNG, ECompression.PNG, new String[]{"png"}, true),
	GIF(ECompression.GIF, ECompression.PNG, new String[]{"gif"}, true),
	SVG(ECompression.PNG, ECompression.PNG, new String[]{"svg"}, true),
	PSD(ECompression.PNG, ECompression.PNG, new String[]{"psd"}, true), //adobe photoshop
	TIFF(ECompression.TIFF, ECompression.PNG, new String[]{"tif", "tiff"}, true), //Tagged Image File Format
	BMP(ECompression.BMP, ECompression.PNG, new String[]{"bmp"}, true); // bitmap image file or device independent bitmap (DIB)

	public final ECompression outCompressionStrict;
	public final ECompression outCompressionCompat;
	public final String[] extensions;
	public final boolean supportRead;

	ImageType(ECompression outCompressionStrict, ECompression outCompressionCompat, String[] extensions, boolean supportRead) {
		this.outCompressionStrict = outCompressionStrict;
		this.outCompressionCompat = outCompressionCompat;
		this.extensions = extensions;
		this.supportRead = supportRead;
	}

	/**
	 * Supported image compression types
	 */
	public enum ECompression {
		JPG(false, "jpg"), PNG(true, "png"), GIF(true, "gif"), TIFF(false, "tif"), BMP(false, "bmp");

		public boolean hasTransparency;
		public String extension;

		ECompression(boolean hasTransparency, String extension) {
			this.hasTransparency = hasTransparency;
			this.extension = extension;
		}
	}
}
