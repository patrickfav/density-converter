/*
 *  Copyright 2016 Patrick Favre-Bulle
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package at.favre.tools.dconvert.converters;

import at.favre.tools.dconvert.arg.Arguments;
import at.favre.tools.dconvert.arg.EScaleMode;
import at.favre.tools.dconvert.arg.ImageType;
import at.favre.tools.dconvert.converters.descriptors.DensityDescriptor;
import at.favre.tools.dconvert.converters.scaling.ImageHandler;
import at.favre.tools.dconvert.util.DensityBucketUtil;
import at.favre.tools.dconvert.util.ImageUtil;
import at.favre.tools.dconvert.util.LoadedImage;
import at.favre.tools.dconvert.util.MiscUtil;

import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.List;

/**
 * The main logic of all platform converters
 */
public abstract class APlatformConverter<T extends DensityDescriptor> implements IPlatformConverter {

    @Override
    public Result convert(File srcImage, Arguments args) {
        try {
            File destinationFolder = args.dst;
            LoadedImage imageData = ImageUtil.loadImage(srcImage);
            String targetImageFileName = MiscUtil.getFileNameWithoutExtension(srcImage);
            ImageType imageType = Arguments.getImageType(srcImage);
            boolean isNinePatch = AndroidConverter.isNinePatch(srcImage) && getClass() == AndroidConverter.class;

            StringBuilder log = new StringBuilder();
            log.append(getConverterName()).append(": ").append(targetImageFileName).append(" ")
                    .append(imageData.getImage().getWidth()).append("x").append(imageData.getImage().getHeight()).append(" (").append(args.scale).append(args.scaleMode == EScaleMode.FACTOR ? "x" : "dp").append(")\n");

            Map<T, Dimension> densityMap = DensityBucketUtil.getDensityBuckets(usedOutputDensities(args), new Dimension(imageData.getImage().getWidth(), imageData.getImage().getHeight()), args, args.scale, isNinePatch);

            File mainSubFolder = createMainSubFolder(destinationFolder, targetImageFileName, args);

            onPreExecute(mainSubFolder, targetImageFileName, usedOutputDensities(args), imageType, args);

            List<File> allResultingFiles = new ArrayList<>();

            for (Map.Entry<T, Dimension> entry : densityMap.entrySet()) {
                File dstFolder = createFolderForOutputFile(mainSubFolder, entry.getKey(), entry.getValue(), targetImageFileName, args);

                if ((dstFolder.isDirectory() && dstFolder.exists()) || args.dryRun) {
                    File imageFile = new File(dstFolder, createDestinationFileNameWithoutExtension(entry.getKey(), entry.getValue(), targetImageFileName, args));

                    log.append("process ").append(imageFile).append(" with ").append(entry.getValue().width).append("x").append(entry.getValue().height).append(" (x")
                            .append(entry.getKey().scale).append(") ").append(isNinePatch ? "(9-patch)" : "").append("\n");

                    if (!args.dryRun) {
                        List<File> files = new ImageHandler(args).saveToFile(imageFile, imageData, entry.getValue(), isNinePatch);

                        allResultingFiles.addAll(files);

                        for (File file : files) {
                            log.append("compressed to disk: ").append(file).append(" (").append(String.format(Locale.US, "%.2f", (float) file.length() / 1024f)).append("kB)\n");
                        }

                        if (files.isEmpty()) {
                            log.append("files skipped\n");
                        }
                    }
                } else {
                    throw new IllegalStateException("could not create " + dstFolder);
                }
            }

            onPostExecute(args);

            imageData.getImage().flush();

            return new Result(log.toString(), allResultingFiles);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(null, e, Collections.emptyList());
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
