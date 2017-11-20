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
import at.favre.tools.dconvert.arg.ImageType;
import at.favre.tools.dconvert.converters.descriptors.AndroidDensityDescriptor;
import at.favre.tools.dconvert.util.MiscUtil;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Converts and creates Android-style resource set
 */
public class AndroidConverter extends APlatformConverter<AndroidDensityDescriptor> {

    private static final String ANDROID_FOLDER_NAME = "android";

    @Override
    public List<AndroidDensityDescriptor> usedOutputDensities(Arguments arguments) {
        return getAndroidDensityDescriptors(arguments);
    }

    public static List<AndroidDensityDescriptor> getAndroidDensityDescriptors(Arguments arguments) {
        List<AndroidDensityDescriptor> list = new ArrayList<>();
        String dirPrefix = arguments.createMipMapInsteadOfDrawableDir ? "mipmap" : "drawable";
        if (arguments.includeAndroidLdpiTvdpi) {
            list.add(new AndroidDensityDescriptor(0.75f, "ldpi", dirPrefix + "-ldpi"));
            list.add(new AndroidDensityDescriptor(1.33f, "tvdpi", dirPrefix + "-tvdpi"));
        }
        list.add(new AndroidDensityDescriptor(1, "mdpi", dirPrefix + "-mdpi"));
        list.add(new AndroidDensityDescriptor(1.5f, "hdpi", dirPrefix + "-hdpi"));
        list.add(new AndroidDensityDescriptor(2, "xhdpi", dirPrefix + "-xhdpi"));
        list.add(new AndroidDensityDescriptor(3, "xxhdpi", dirPrefix + "-xxhdpi"));
        list.add(new AndroidDensityDescriptor(4, "xxxhdpi", dirPrefix + "-xxxhdpi"));
        return list;
    }

    @Override
    public String getConverterName() {
        return "android-converter";
    }

    @Override
    public File createMainSubFolder(File destinationFolder, String targetImageFileName, Arguments arguments) {
        if (arguments.platform.size() > 1) {
            return MiscUtil.createAndCheckFolder(new File(destinationFolder, ANDROID_FOLDER_NAME).getAbsolutePath(), arguments.dryRun);
        } else {
            return destinationFolder;
        }
    }

    @Override
    public File createFolderForOutputFile(File mainSubFolder, AndroidDensityDescriptor density, Dimension dimension, String targetFileName, Arguments arguments) {
        return MiscUtil.createAndCheckFolder(new File(mainSubFolder, density.folderName).getAbsolutePath(), arguments.dryRun);
    }

    @Override
    public String createDestinationFileNameWithoutExtension(AndroidDensityDescriptor density, Dimension dimension, String targetFileName, Arguments arguments) {
        return targetFileName;
    }

    @Override
    public void onPreExecute(File dstFolder, String targetFileName, List<AndroidDensityDescriptor> densityDescriptions, ImageType imageType, Arguments arguments) throws Exception {
        //nothing
    }

    @Override
    public void onPostExecute(Arguments arguments) {
        //nothing
    }

    public static boolean isNinePatch(File file) {
        return file.getName().endsWith(".9.png");
    }

    @Override
    public void clean(Arguments arguments) {
        if (arguments.platform.size() == 1) {
            for (AndroidDensityDescriptor androidDensityDescriptor : getAndroidDensityDescriptors(arguments)) {
                File dir = new File(arguments.dst, androidDensityDescriptor.folderName);
                MiscUtil.deleteFolder(dir);
            }
        } else {
            MiscUtil.deleteFolder(new File(arguments.dst, ANDROID_FOLDER_NAME));
        }
    }
}
