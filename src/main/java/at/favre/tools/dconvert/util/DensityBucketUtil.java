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
import at.favre.tools.dconvert.arg.EScaleType;
import at.favre.tools.dconvert.arg.ImageType;
import at.favre.tools.dconvert.converters.descriptors.DensityDescriptor;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

/**
 * Helps assembling needed densities to convert to
 */
public class DensityBucketUtil {
	public static final float SVG_UPSCALE_FACTOR = 4;

	public static <T extends DensityDescriptor> Map<T, Dimension> getDensityBuckets(File srcFile, java.util.List<T> densities, Dimension dimensionForScalingFactor, Arguments args, float scale) throws IOException {
		switch (args.scaleType) {
			default:
			case FACTOR:
				return getDensityBucketsWithFactorScale(densities, dimensionForScalingFactor, args, scale);
			case DP_WIDTH:
				return getDensityBucketsWithDpScale(srcFile, densities, args, scale);
			case DP_HEIGHT:
				return getDensityBucketsHeightDpScale(srcFile, densities, args, scale);
		}
	}

	private static <T extends DensityDescriptor> Map<T, Dimension> getDensityBucketsWithDpScale(File srcFile, java.util.List<T> densities, Arguments args, float scale) throws IOException {
		Dimension srcDimension = ImageUtil.getImageDimension(srcFile);
		float scaleFactor = scale / (float) srcDimension.width;

		int baseWidth = (int) args.round(scale);
		int baseHeight = (int) args.round(scaleFactor * (float) srcDimension.height);

		Map<T, Dimension> bucketMap = new TreeMap<>();
		densities.stream().filter(density -> (int) args.round(baseWidth * density.scale) <= srcDimension.width || !args.skipUpscaling).forEach(density -> {
			bucketMap.put(density, new Dimension((int) args.round(baseWidth * density.scale),
					(int) args.round(baseHeight * density.scale)));
		});
		return bucketMap;
	}

	private static <T extends DensityDescriptor> Map<T, Dimension> getDensityBucketsHeightDpScale(File srcFile, java.util.List<T> densities, Arguments args, float scale) throws IOException {
		Dimension srcDimension = ImageUtil.getImageDimension(srcFile);
		float scaleFactor = scale / (float) srcDimension.height;

		int baseWidth = (int) args.round(scaleFactor * (float) srcDimension.width);
		int baseHeight = (int) args.round(scale);

		Map<T, Dimension> bucketMap = new TreeMap<>();
		densities.stream().filter(density -> (int) args.round(baseHeight * density.scale) <= srcDimension.height || !args.skipUpscaling).forEach(density -> {
			bucketMap.put(density, new Dimension((int) args.round(baseWidth * density.scale),
					(int) args.round(baseHeight * density.scale)));
		});
		return bucketMap;
	}

	private static <T extends DensityDescriptor> Map<T, Dimension> getDensityBucketsWithFactorScale(java.util.List<T> densities, Dimension dimensionForScalingFactor, Arguments args, float scale) {
		double baseWidth = (double) dimensionForScalingFactor.width / scale;
		double baseHeight = (double) dimensionForScalingFactor.height / scale;

		Map<T, Dimension> bucketMap = new TreeMap<>();
		densities.stream().filter(density -> scale >= density.scale || !args.skipUpscaling).forEach(density -> {
			bucketMap.put(density, new Dimension((int) args.round(baseWidth * density.scale),
					(int) args.round(baseHeight * density.scale)));
		});
		return bucketMap;
	}

	public static BufferedImage getAsBufferedImage(File image, Arguments args) throws Exception {
		if (Arguments.getImageType(image) == ImageType.SVG) {
			return ImageUtil.readSvg(image, getHqDimension(image, args));
		} else {
			return ImageUtil.loadImage(image);
		}
	}

	private static Dimension getHqDimension(File image, Arguments args) throws IOException {
		Dimension srcDimension = ImageUtil.getImageDimension(image);
		Dimension hqDimension;
		if (args.scaleType == EScaleType.FACTOR && args.scale < SVG_UPSCALE_FACTOR) {
			hqDimension = new Dimension((int) args.round(SVG_UPSCALE_FACTOR / args.scale * (float) srcDimension.width), (int) args.round(SVG_UPSCALE_FACTOR / args.scale * (float) srcDimension.width));
		} else if (args.scaleType == EScaleType.DP_WIDTH && (args.scale * SVG_UPSCALE_FACTOR < srcDimension.width)) {
			float scaleFactor = args.scale / (float) srcDimension.width * SVG_UPSCALE_FACTOR;
			hqDimension = new Dimension((int) args.round(scaleFactor * (float) srcDimension.width), (int) args.round(scaleFactor * (float) srcDimension.height));
		} else if (args.scaleType == EScaleType.DP_HEIGHT && (args.scale * SVG_UPSCALE_FACTOR < srcDimension.height)) {
			float scaleFactor = args.scale / (float) srcDimension.height * SVG_UPSCALE_FACTOR;
			hqDimension = new Dimension((int) args.round(scaleFactor * (float) srcDimension.width), (int) args.round(scaleFactor * (float) srcDimension.height));
		} else {
			hqDimension = srcDimension;
		}
		return hqDimension;
	}
}
