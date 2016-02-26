package at.favre.tools.converter.platforms;

import at.favre.tools.converter.Arguments;
import at.favre.tools.converter.ConverterUtil;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Shared code
 */
public abstract class APlatformConverter<T extends DensityDescription> implements IPlatformConverter {

	private Map<T, Dimension> getDensityBuckets(List<T> densities, Dimension dimensionForScalingFactor, Arguments args) {
		double baseWidth = (double) dimensionForScalingFactor.width / args.scrScale;
		double baseHeight = (double) dimensionForScalingFactor.height / args.scrScale;

		Map<T, Dimension> bucketMap = new TreeMap<>();
		for (T density : densities) {
			bucketMap.put(density, new Dimension((int) args.roundingHandler.round(baseWidth * density.scale),
					(int) args.roundingHandler.round(baseHeight * density.scale)));
		}
		return bucketMap;
	}

	@Override
	public void convert(File destinationFolder, BufferedImage rawImage, String targetImageFileName, Arguments.Compression srcCompression, Arguments args, ConverterCallback callback) {
		try {
			StringBuilder log = new StringBuilder();
			log.append(getConverterName()).append(": ").append(targetImageFileName).append(" ").append(rawImage.getWidth()).append("x").append(rawImage.getHeight()).append(" (x").append(args.scrScale).append(")\n");

			Map<T, Dimension> densityMap = getDensityBuckets(usedOutputDensities(args), new Dimension(rawImage.getWidth(), rawImage.getHeight()), args);

			File mainSubFolder = createMainSubFolder(destinationFolder, targetImageFileName, args);

			onPreExecute(mainSubFolder, targetImageFileName, usedOutputDensities(args), args);

			for (Map.Entry<T, Dimension> entry : densityMap.entrySet()) {
				File dstFolder = createFolderForOutputFile(mainSubFolder, entry.getKey(), entry.getValue(), targetImageFileName, args);

				if (dstFolder.isDirectory() && dstFolder.exists()) {
					File imageFile = new File(dstFolder, createDestinationFileNameWithoutExtension(entry.getKey(), entry.getValue(), targetImageFileName, args));

					if (imageFile.exists()) {
						imageFile.delete();
					}

					log.append("create ").append(imageFile).append(" with ").append(entry.getValue().width).append("x").append(entry.getValue().height).append(" (x").append(entry.getKey().scale).append(")\n");

					ConverterUtil.compressToFile(imageFile, Arguments.getCompressionForType(args.outputCompressionMode, srcCompression), rawImage, entry.getValue(), args.compressionQuality);
				} else {
					throw new IllegalStateException("could not create " + dstFolder);
				}
			}

			onPostExecute(args);

			if (callback != null) {
				callback.success(log.toString());
			}
		} catch (Exception e) {
			if (callback != null) {
				callback.failure(e);
			} else {
				e.printStackTrace();
			}
		}
	}

	public abstract List<T> usedOutputDensities(Arguments arguments);

	public abstract String getConverterName();

	public abstract File createMainSubFolder(File destinationFolder, String targetImageFileName, Arguments arguments);

	public abstract File createFolderForOutputFile(File mainSubFolder, T density, Dimension dimension, String targetFileName, Arguments arguments);

	public abstract String createDestinationFileNameWithoutExtension(T density, Dimension dimension, String targetFileName, Arguments arguments);

	public abstract void onPreExecute(File dstFolder, String targetFileName, List<T> densityDescriptions, Arguments arguments) throws Exception;

	public abstract void onPostExecute(Arguments arguments);
}
