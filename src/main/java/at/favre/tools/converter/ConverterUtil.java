package at.favre.tools.converter;

import at.favre.tools.converter.graphics.CompressionType;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Created by PatrickF on 25.02.2016.
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
		return file.getName().substring(file.getName().lastIndexOf(".") + 1);
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

	public static void compressToFile(File targetFile, CompressionType type, BufferedImage bufferedImage, Dimension targetDimension) throws Exception {
		if(type == CompressionType.JPEG) {
			compressJpeg(targetFile,scale(bufferedImage, targetDimension.width, targetDimension.height, type, Color.BLACK),0.9f);
		} else {
			ImageIO.write(scale(bufferedImage, targetDimension.width, targetDimension.height, type, Color.BLACK), type.formatName, targetFile);
		}
	}

	public static void compressJpeg(File targetFile,BufferedImage bufferedImage, float quality) throws IOException {
		ImageWriter jpgWriter = ImageIO.getImageWritersByFormatName("jpg").next();
		ImageWriteParam jpgWriteParam = jpgWriter.getDefaultWriteParam();
		jpgWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
		jpgWriteParam.setCompressionQuality(quality);

		final ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
		writer.setOutput(new FileImageOutputStream(targetFile));

		writer.write(null, new IIOImage(bufferedImage, null, null), jpgWriteParam);
	}

	public static BufferedImage scale(BufferedImage imageToScale, int dWidth, int dHeight, CompressionType compressionType, Color background) {
		BufferedImage scaledImage = null;
		if (imageToScale != null) {
			int imageType = imageToScale.getType();
			if (compressionType == CompressionType.PNG || compressionType == CompressionType.GIF) {
				imageType = BufferedImage.TYPE_INT_ARGB;
			}

			scaledImage = new BufferedImage(dWidth, dHeight, imageType);
			Graphics2D graphics2D = scaledImage.createGraphics();
			graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			if (compressionType == CompressionType.JPEG) {
				graphics2D.drawImage(imageToScale, 0, 0, dWidth, dHeight, background, null);
			} else {
				graphics2D.drawImage(imageToScale, 0, 0, dWidth, dHeight, null);
			}

			graphics2D.dispose();
		}
		return scaledImage;
	}

	private static void draw() {

	}
}
