package at.favre.tools.converter;

import at.favre.tools.converter.arg.ECompression;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Util class
 */
public class ConverterUtil {

	public static String getWithoutExtension(File file) {
		String fileName = file.getName();
		int pos = fileName.lastIndexOf(".");
		if (pos > 0) {
			fileName = fileName.substring(0, pos);
		}
		return fileName;
	}

	public static String getFileExtension(File file) {
		return file.getName().substring(file.getName().lastIndexOf(".") + 1).toLowerCase();
	}

	public static File createAndCheckFolder(String path) {
		File f = new File(path);

		if (!f.exists()) {
			f.mkdir();
		}

		if (!f.exists() || !f.isDirectory()) {
			throw new IllegalStateException("could not create folder: " + path);
		}
		return f;
	}

	public static BufferedImage loadImage(String filePath) throws Exception {
		return ImageIO.read(new File(filePath));
	}

	public static List<File> compressToFile(File targetFile, List<ECompression> compressionList, BufferedImage bufferedImage, Dimension targetDimension, float compressionQuality, boolean skipIfExists) throws Exception {
		List<File> files = new ArrayList<>(2);
		for (ECompression compression : compressionList) {
			File imageFile = new File(targetFile.getAbsolutePath() + "." + compression.name().toLowerCase());

			if (imageFile.exists() && skipIfExists) {
				break;
			}

			if (compression == ECompression.PNG || compression == ECompression.GIF) {
				ImageIO.write(scale(bufferedImage, targetDimension.width, targetDimension.height, compression, Color.BLACK), compression.name().toLowerCase(), imageFile);
			} else if (compression == ECompression.JPG) {
				compressJpeg(imageFile, scale(bufferedImage, targetDimension.width, targetDimension.height, compression, Color.BLACK), compressionQuality);
			}
			files.add(imageFile);
		}
		return files;
	}

	public static void compressJpeg(File targetFile,BufferedImage bufferedImage, float quality) throws IOException {
		ImageWriter jpgWriter = ImageIO.getImageWritersByFormatName("jpg").next();
		ImageWriteParam jpgWriteParam = jpgWriter.getDefaultWriteParam();
		jpgWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
		jpgWriteParam.setCompressionQuality(quality);

		ImageWriter writer = null;
		try (ImageOutputStream outputStream = new FileImageOutputStream(targetFile)) {
			writer = ImageIO.getImageWritersByFormatName("jpg").next();
			writer.setOutput(outputStream);
			writer.write(null, new IIOImage(bufferedImage, null, null), jpgWriteParam);
		} finally {
			if (writer != null) writer.dispose();
		}
	}

	public static BufferedImage scale(BufferedImage imageToScale, int dWidth, int dHeight, ECompression compression, Color background) {
		BufferedImage scaledImage = null;
		if (imageToScale != null) {
			int imageType = imageToScale.getType();
			if (compression == ECompression.PNG || compression == ECompression.GIF) {
				imageType = BufferedImage.TYPE_INT_ARGB;
			}

			scaledImage = new BufferedImage(dWidth, dHeight, imageType);
			Graphics2D graphics2D = scaledImage.createGraphics();
			graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			graphics2D.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
			graphics2D.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
			graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);


			if (compression == ECompression.JPG) {
				graphics2D.drawImage(imageToScale, 0, 0, dWidth, dHeight, null);
			} else {
				graphics2D.drawImage(imageToScale, 0, 0, dWidth, dHeight, null);
			}

			graphics2D.dispose();
		}
		return scaledImage;
	}
}
