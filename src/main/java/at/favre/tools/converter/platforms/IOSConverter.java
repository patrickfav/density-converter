package at.favre.tools.converter.platforms;

import at.favre.tools.converter.ConverterUtil;
import at.favre.tools.converter.graphics.CompressionType;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.TreeMap;

/**
 * Converts and creates Android-style resource set
 */
public class IOSConverter extends APlatformConverter {

	private enum Resolutions {
		X1(1, "","1x"),
		X2(2, "_2x","2x"),
		X3(3, "_3x","3x");

		public final double scalingFactor;
		public final String postFix;
		public final String scaleDescr;

		Resolutions(double scalingFactor, String postFix, String scaleDescr) {
			this.scalingFactor = scalingFactor;
			this.postFix = postFix;
			this.scaleDescr = scaleDescr;
		}
	}

	private Map<Resolutions, Dimension> getResolutions(Dimension dimensionForScalingFactor, double baseScalingFactor) {
		double baseWidth = (double) dimensionForScalingFactor.width / baseScalingFactor;
		double baseHeight = (double) dimensionForScalingFactor.height / baseScalingFactor;

		Map<Resolutions, Dimension> resolutionMap = new TreeMap<>();
		for (Resolutions res : Resolutions.values()) {
			resolutionMap.put(res, new Dimension((int) roundingHandler.round(baseWidth * res.scalingFactor),
					(int) roundingHandler.round(baseHeight * res.scalingFactor)));
		}
		return resolutionMap;
	}

	public void convert(File targetRootFolder, BufferedImage rawImage, double baseScale, String targetImageFileName, CompressionType compressionType, float quality) throws Exception {
		StringBuilder log = new StringBuilder();

		log.append("ios-converter: ").append(targetImageFileName).append(" ").append(rawImage.getWidth()).append("x").append(rawImage.getHeight()).append(" (x").append(baseScale).append(")\n");

		Map<Resolutions, Dimension> resMap = getResolutions(new Dimension(rawImage.getWidth(), rawImage.getHeight()), baseScale);

		File resFolder = ConverterUtil.createAndCheckFolder(new File(targetRootFolder, "ios").getAbsolutePath());
		File dstFolder = ConverterUtil.createAndCheckFolder(new File(resFolder, targetImageFileName + ".imageset").getAbsolutePath());

		log.append("create Content.json for ").append(targetImageFileName).append("\n");
		writeContentJson(dstFolder,targetImageFileName,compressionType);


		for (Map.Entry<Resolutions, Dimension> entry : resMap.entrySet()) {

			if (dstFolder.isDirectory() && dstFolder.exists()) {
				File imageFile = new File(dstFolder, targetImageFileName + entry.getKey().postFix + "." + compressionType.formatName);

				if (imageFile.exists()) {
					imageFile.delete();
				}

				log.append("create ").append(imageFile).append(" with ").append(entry.getValue().width).append("x").append(entry.getValue().height).append(" (x").append(entry.getKey().scalingFactor).append(")\n");

				ConverterUtil.compressToFile(imageFile, compressionType, rawImage, entry.getValue());
			} else {
				throw new IllegalStateException("could not create " + dstFolder);
			}
		}

		System.out.println(log.toString());
	}

	private void writeContentJson(File dstFolder, String targetFileName, CompressionType compressionType) throws IOException {
		File contentJson = new File(dstFolder, "Content.json");

		if (contentJson.exists()) {
			contentJson.delete();
		}
		contentJson.createNewFile();

		try (PrintWriter out = new PrintWriter(contentJson)) {
			out.println(createContentJson(targetFileName, compressionType));
		}
	}

	private String createContentJson(String targetFileName, CompressionType compressionType) {
		StringBuilder sb = new StringBuilder("{\n\t\"images\": [");

		for (Resolutions resolutions : Resolutions.values()) {
			sb.append("\n\t\t{\n" +
					"\t\t\t\"filename\": \"" + targetFileName + resolutions.postFix + "." + compressionType.formatName + "\",\n" +
					"\t\t\t\"idiom\": \"universal\",\n" +
					"\t\t\t\"scale\": \"" + resolutions.scaleDescr + "\"\n" +
					"\t\t},");
		}
		sb.setLength(sb.length()-1);
		sb.append("\t],\n\t\"info\": {\n\t\t\"author\": \"xcode\",\n\t\t\"version\": 1\n\t}\n}");

		return sb.toString();
	}
}