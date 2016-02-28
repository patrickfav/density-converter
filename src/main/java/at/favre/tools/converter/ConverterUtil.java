package at.favre.tools.converter;

import at.favre.tools.converter.arg.Arguments;
import at.favre.tools.converter.arg.ECompression;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Util class
 */
public class ConverterUtil {

	public static String getFileNameWithoutExtension(File file) {
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

	public static String runWebP(File target, String[] additionalArgs, File outFile) {
		String[] cmdArray = concat(concat(new String[]{"cwebp"}, additionalArgs), new String[]{"\"" + target.getAbsoluteFile() + "\"", "-o", "\"" + outFile.getAbsoluteFile() + "\""});
		return runCmd(cmdArray);
	}

	public static String runPngCrush(File target, String[] additionalArgs) {
		if (Arguments.getCompressionType(target) == ECompression.PNG && target.exists() && target.isFile()) {
			String[] cmdArray = concat(concat(new String[]{"pngcrush"}, additionalArgs), new String[]{"-ow", "\"" + target.getAbsoluteFile() + "\""});
			return runCmd(cmdArray);
		}
		return "";
	}

	private static String runCmd(String[] cmdArray) {
		StringBuilder logStringBuilder = new StringBuilder();
		try {
			logStringBuilder.append("execute: ").append(Arrays.toString(cmdArray)).append("\n");
			ProcessBuilder pb = new ProcessBuilder(cmdArray);
			pb.redirectErrorStream(true);
			Process process = pb.start();
			try (BufferedReader inStreamReader = new BufferedReader(
					new InputStreamReader(process.getInputStream()))) {
				String s;
				while ((s = inStreamReader.readLine()) != null) {
					logStringBuilder.append("\t").append(s).append("\n");
				}
			}
		} catch (Exception e) {
			logStringBuilder.append("error: could not png crush - ").append(e.getMessage()).append(" - is it set in PATH?\n");
		}
		return logStringBuilder.toString();

	}

	public static <T> T[] concat(T[] first, T[] second) {
		T[] result = Arrays.copyOf(first, first.length + second.length);
		System.arraycopy(second, 0, result, first.length, second.length);
		return result;
	}

	public static List<File> compressToFile(File targetFile, List<ECompression> compressionList, BufferedImage bufferedImage, Dimension targetDimension,
	                                        float compressionQuality, boolean skipIfExists) throws Exception {
		List<File> files = new ArrayList<>(2);
		for (ECompression compression : compressionList) {
			File imageFile = new File(targetFile.getAbsolutePath() + "." + compression.name().toLowerCase());

			if (imageFile.exists() && skipIfExists) {
				break;
			}

			BufferedImage scaledImage = scale(bufferedImage, targetDimension.width, targetDimension.height, compression, Color.BLACK);

			if (compression == ECompression.PNG || compression == ECompression.GIF) {
				ImageIO.write(scaledImage, compression.name().toLowerCase(), imageFile);
			} else if (compression == ECompression.JPG) {
				compressJpeg(imageFile, scaledImage, compressionQuality);
			}
			scaledImage.flush();
			files.add(imageFile);
		}
		return files;
	}

	public static void compressJpeg(File targetFile, BufferedImage bufferedImage, float quality) throws IOException {
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
