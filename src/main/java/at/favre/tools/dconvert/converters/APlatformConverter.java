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

package at.favre.tools.dconvert.converters;

import at.favre.tools.dconvert.arg.Arguments;
import at.favre.tools.dconvert.arg.EScaleType;
import at.favre.tools.dconvert.arg.ImageType;
import at.favre.tools.dconvert.converters.descriptors.DensityDescriptor;
import at.favre.tools.dconvert.util.ImageUtil;
import at.favre.tools.dconvert.util.MiscUtil;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * The main logic of all platform converters
 */
public abstract class APlatformConverter<T extends DensityDescriptor> implements IPlatformConverter {

	private Map<T, Dimension> getDensityBuckets(File srcFile, List<T> densities, Dimension dimensionForScalingFactor, Arguments args) throws IOException {
		switch (args.scaleType) {
			default:
			case FACTOR:
				return getDensityBucketsWithFactorScale(densities, dimensionForScalingFactor, args);
			case DP:
				return getDensityBucketsWithDpScale(srcFile, densities, args);
		}
	}

	private Map<T, Dimension> getDensityBucketsWithDpScale(File srcFile, List<T> densities, Arguments args) throws IOException {
		Dimension srcDimension = ImageUtil.getImageDimension(srcFile);
		float scaleFactor = args.scale / (float) srcDimension.width;

		int baseWidth = (int) args.round(args.scale);
		int baseHeight = (int) args.round(scaleFactor * (float) srcDimension.height);

		return getDimensionMap(densities, args, baseWidth, baseHeight);
	}

	private Map<T, Dimension> getDensityBucketsWithFactorScale(List<T> densities, Dimension dimensionForScalingFactor, Arguments args) {
		double baseWidth = (double) dimensionForScalingFactor.width / args.scale;
		double baseHeight = (double) dimensionForScalingFactor.height / args.scale;

		return getDimensionMap(densities, args, baseWidth, baseHeight);
	}

	private Map<T, Dimension> getDimensionMap(List<T> densities, Arguments args, double baseWidth, double baseHeight) {
		Map<T, Dimension> bucketMap = new TreeMap<>();
		densities.stream().filter(density -> args.scale >= density.scale || !args.skipUpscaling).forEach(density -> {
			bucketMap.put(density, new Dimension((int) args.round(baseWidth * density.scale),
					(int) args.round(baseHeight * density.scale)));
		});
		return bucketMap;
	}

	@Override
	public void convert(File srcImage, Arguments args, ConverterCallback callback) {
		try {
			File destinationFolder = args.dst;
			BufferedImage rawImage = ImageUtil.loadImage(srcImage.getAbsolutePath());
			String targetImageFileName = MiscUtil.getFileNameWithoutExtension(srcImage);
			ImageType imageType = Arguments.getImageType(srcImage);

			StringBuilder log = new StringBuilder();
			log.append(getConverterName()).append(": ").append(targetImageFileName).append(" ")
					.append(rawImage.getWidth()).append("x").append(rawImage.getHeight()).append(" (").append(args.scale).append(args.scaleType == EScaleType.FACTOR ? "x" : "dp").append(")\n");

			Map<T, Dimension> densityMap = getDensityBuckets(srcImage, usedOutputDensities(args), new Dimension(rawImage.getWidth(), rawImage.getHeight()), args);

			File mainSubFolder = createMainSubFolder(destinationFolder, targetImageFileName, args);

			onPreExecute(mainSubFolder, targetImageFileName, usedOutputDensities(args), imageType, args);

			List<File> allResultingFiles = new ArrayList<>();

			for (Map.Entry<T, Dimension> entry : densityMap.entrySet()) {
				File dstFolder = createFolderForOutputFile(mainSubFolder, entry.getKey(), entry.getValue(), targetImageFileName, args);

				if (dstFolder.isDirectory() && dstFolder.exists()) {
					File imageFile = new File(dstFolder, createDestinationFileNameWithoutExtension(entry.getKey(), entry.getValue(), targetImageFileName, args));

					log.append("process ").append(imageFile).append(" with ").append(entry.getValue().width).append("x").append(entry.getValue().height).append(" (x").append(entry.getKey().scale).append(")\n");

					List<File> files = ImageUtil.compressToFile(imageFile, Arguments.getOutCompressionForType(args.outputCompressionMode, imageType), rawImage,
							entry.getValue(), args.compressionQuality, args.skipExistingFiles, args.enableAntiAliasing);

					allResultingFiles.addAll(files);

					for (File file : files) {
						log.append("compressed to disk: ").append(file).append(" (").append(String.format(Locale.US, "%.2f", (float) file.length() / 1024f)).append("kB)\n");
					}

					if (files.isEmpty()) {
						log.append("files skipped\n");
					}
				} else {
					throw new IllegalStateException("could not create " + dstFolder);
				}
			}

			onPostExecute(args);

			rawImage.flush();
			rawImage = null;

			if (callback != null) {
				callback.success(log.toString(), allResultingFiles);
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

	public abstract void onPreExecute(File dstFolder, String targetFileName, List<T> densityDescriptions, ImageType imageType, Arguments arguments) throws Exception;

	public abstract void onPostExecute(Arguments arguments);
}
