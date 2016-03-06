/*
 * Copyright (C) 2016 Patrick Favre-Bulle
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package at.favre.tools.dconvert.util;

import at.favre.tools.dconvert.arg.Arguments;
import at.favre.tools.dconvert.arg.ImageType;
import org.imgscalr.Scalr;

import javax.imageio.*;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Main Util class containing all
 */
public class ImageUtil {

	public static BufferedImage loadImage(File file) throws Exception {
		return ImageIO.read(file);
	}

	public static String runWebP(File target, String[] additionalArgs, File outFile) {
		String[] cmdArray = MiscUtil.concat(MiscUtil.concat(new String[]{"cwebp"}, additionalArgs), new String[]{"\"" + target.getAbsoluteFile() + "\"", "-o", "\"" + outFile.getAbsoluteFile() + "\""});
		return runCmd(cmdArray);
	}

	public static String runPngCrush(File target, String[] additionalArgs) {
		if (Arguments.getImageType(target) == ImageType.PNG && target.exists() && target.isFile()) {
			String[] cmdArray = MiscUtil.concat(MiscUtil.concat(new String[]{"pngcrush"}, additionalArgs), new String[]{"-ow", "\"" + target.getAbsoluteFile() + "\""});
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
			process.waitFor();
		} catch (Exception e) {
			logStringBuilder.append("error: could not run command - ").append(Arrays.toString(cmdArray)).append(" - ").append(e.getMessage()).append(" - is it set in PATH?\n");
		}
		return logStringBuilder.toString();

	}

	public static List<File> compressToFile(File targetFile, List<ImageType.ECompression> compressionList, BufferedImage bufferedImage, Dimension targetDimension,
	                                        float compressionQuality, boolean skipIfExists, boolean antiAlias) throws Exception {
		List<File> files = new ArrayList<>(2);
		for (ImageType.ECompression compression : compressionList) {
			File imageFile = new File(targetFile.getAbsolutePath() + "." + compression.extension);

			if (imageFile.exists() && skipIfExists) {
				break;
			}

			BufferedImage scaledImage = scale(bufferedImage, targetDimension.width, targetDimension.height, compression, Color.white, antiAlias);

			if (compression == ImageType.ECompression.JPG) {
				compressJpeg(imageFile, scaledImage, compressionQuality);
			} else {
				ImageIO.write(scaledImage, compression.name().toLowerCase(), imageFile);
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

	public static BufferedImage scale(BufferedImage imageToScale, int dWidth, int dHeight, ImageType.ECompression compression, Color background, boolean antiAlias) {
		BufferedImage scaledImage = null;
		if (imageToScale != null) {

			BufferedImageOp[] bufferedImageOpArray = new BufferedImageOp[]{};

			if (antiAlias) {
				bufferedImageOpArray = new BufferedImageOp[]{Scalr.OP_ANTIALIAS};
			}

			scaledImage = Scalr.resize(imageToScale, Scalr.Method.ULTRA_QUALITY, Scalr.Mode.FIT_EXACT, dWidth, dHeight, bufferedImageOpArray);

			if (!compression.hasTransparency) {
				BufferedImage convertedImg = new BufferedImage(scaledImage.getWidth(), scaledImage.getHeight(), BufferedImage.TYPE_INT_RGB);
				convertedImg.getGraphics().drawImage(scaledImage, 0, 0, background, null);
				scaledImage = convertedImg;
			}
		}
		return scaledImage;
	}

	@Deprecated
	public static BufferedImage readSvg(File file, Dimension sourceDimension) throws Exception {
		try (ImageInputStream input = ImageIO.createImageInputStream(file)) {
			Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
			if (!readers.hasNext()) {
				throw new IllegalArgumentException("No reader for: " + file);
			}

			ImageReader reader = readers.next();
			try {
				reader.setInput(input);
				ImageReadParam param = reader.getDefaultReadParam();
				param.setSourceRenderSize(sourceDimension);
				return reader.read(0, param);
			} finally {
				reader.dispose();
			}
		}
	}

	/**
	 * Gets image dimensions for given file
	 *
	 * @param imgFile image file
	 * @return dimensions of image
	 * @throws IOException if the file is not a known image
	 */
	public static Dimension getImageDimension(File imgFile) throws IOException {
		int pos = imgFile.getName().lastIndexOf(".");
		if (pos == -1)
			throw new IOException("No extension for file: " + imgFile.getAbsolutePath());
		String suffix = imgFile.getName().substring(pos + 1);
		Iterator<ImageReader> iter = ImageIO.getImageReadersBySuffix(suffix);
		if (iter.hasNext()) {
			ImageReader reader = iter.next();
			try {
				ImageInputStream stream = new FileImageInputStream(imgFile);
				reader.setInput(stream);
				int width = reader.getWidth(reader.getMinIndex());
				int height = reader.getHeight(reader.getMinIndex());
				return new Dimension(width, height);
			} finally {
				reader.dispose();
			}
		}

		throw new IOException("Not a known image file: " + imgFile.getAbsolutePath());
	}
}
