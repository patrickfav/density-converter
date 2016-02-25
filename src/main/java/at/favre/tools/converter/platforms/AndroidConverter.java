package at.favre.tools.converter.platforms;

import at.favre.tools.converter.ConverterUtil;
import at.favre.tools.converter.graphics.CompressionType;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Map;
import java.util.TreeMap;

/**
 * Converts and creates Android-style resource set
 */
public class AndroidConverter extends APlatformConverter {

	private enum DensityBucket {
//		LDPI(0.75, "drawable-ldpi"),
		MDPI(1, "drawable-mdpi"),
		HDPI(1.5, "drawable-hdpi"),
		XHDPI(2, "drawable-xhdpi"),
		XXHDPI(3, "drawable-xxhdpi"),
		XXXHDPI(4, "drawable-xxxhdpi");

		public final double scalingFactor;
		public final String folderName;

		DensityBucket(double scalingFactor, String folderName) {
			this.scalingFactor = scalingFactor;
			this.folderName = folderName;
		}
	}

	private Map<DensityBucket, Dimension> getDensityBuckets(Dimension dimensionForScalingFactor, double baseScalingFactor) {
		double baseWidth = (double) dimensionForScalingFactor.width / baseScalingFactor;
		double baseHeight = (double) dimensionForScalingFactor.height / baseScalingFactor;

		Map<DensityBucket, Dimension> bucketMap = new TreeMap<>();
		for (DensityBucket densityBucket : DensityBucket.values()) {
			bucketMap.put(densityBucket, new Dimension((int) roundingHandler.round(baseWidth * densityBucket.scalingFactor),
					(int) roundingHandler.round(baseHeight * densityBucket.scalingFactor)));
		}
		return bucketMap;
	}

	public void convert(File targetRootFolder, BufferedImage rawImage, double baseScale, String targetImageFileName, CompressionType compressionType, float quality) throws Exception {
		StringBuilder log = new StringBuilder();
		log.append("android-converter: ").append(targetImageFileName).append(" ").append(rawImage.getWidth()).append("x").append(rawImage.getHeight()).append(" (x").append(baseScale).append(")\n");

		Map<DensityBucket, Dimension> bucketMap = getDensityBuckets(new Dimension(rawImage.getWidth(), rawImage.getHeight()), baseScale);

		File resFolder = ConverterUtil.createAndCheckFolder(new File(targetRootFolder,"android").getAbsolutePath());



		for (Map.Entry<DensityBucket, Dimension> entry : bucketMap.entrySet()) {
			File dstFolder = ConverterUtil.createAndCheckFolder(new File(resFolder, entry.getKey().folderName).getAbsolutePath());

			if (dstFolder.isDirectory() && dstFolder.exists()) {
				File imageFile = new File(dstFolder, targetImageFileName + "." + compressionType.formatName);

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
}