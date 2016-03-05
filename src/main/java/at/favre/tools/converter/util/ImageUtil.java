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

package at.favre.tools.converter.util;

import at.favre.tools.converter.arg.Arguments;
import at.favre.tools.converter.arg.ImageType;
import org.imgscalr.Scalr;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
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
import java.util.List;

/**
 * Main Util class containing all
 */
public class ImageUtil {

	public static BufferedImage loadImage(String filePath) throws Exception {
		return ImageIO.read(new File(filePath));
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
			logStringBuilder.append("error: could not png crush - ").append(e.getMessage()).append(" - is it set in PATH?\n");
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

			scaledImage = Scalr.resize(imageToScale, Scalr.Method.ULTRA_QUALITY, dWidth, dHeight, bufferedImageOpArray);

			if (!compression.hasTransparency) {
				BufferedImage convertedImg = new BufferedImage(scaledImage.getWidth(), scaledImage.getHeight(), BufferedImage.TYPE_INT_RGB);
				convertedImg.getGraphics().drawImage(scaledImage, 0, 0, background, null);
				scaledImage = convertedImg;
			}
		}
		return scaledImage;
	}

}
