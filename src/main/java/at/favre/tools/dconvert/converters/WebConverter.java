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
import at.favre.tools.dconvert.converters.descriptors.PostfixDescriptor;
import at.favre.tools.dconvert.util.MiscUtil;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Converts and creates css image-set style images
 */
public class WebConverter extends APlatformConverter<PostfixDescriptor> {
    private static final String WEB_FOLDER_NAME = "web";
    public static final String ROOT_FOLDER = "img";

    @Override
    public List<PostfixDescriptor> usedOutputDensities(Arguments arguments) {
        return getWebDescriptors();
    }

    public static List<PostfixDescriptor> getWebDescriptors() {
        List<PostfixDescriptor> list = new ArrayList<>();
        list.add(new PostfixDescriptor(1, "1x", "-1x"));
        list.add(new PostfixDescriptor(2f, "2x", "-2x"));
        return list;
    }

    @Override
    public String getConverterName() {
        return "web-converter";
    }

    @Override
    public File createMainSubFolder(File destinationFolder, String targetImageFileName, Arguments arguments) {
        if (arguments.platform.size() > 1) {
            destinationFolder = MiscUtil.createAndCheckFolder(new File(destinationFolder, WEB_FOLDER_NAME).getAbsolutePath(), arguments.dryRun);
        }
        return MiscUtil.createAndCheckFolder(new File(destinationFolder, ROOT_FOLDER).getAbsolutePath(), arguments.dryRun);
    }

    @Override
    public File createFolderForOutputFile(File mainSubFolder, PostfixDescriptor density, Dimension dimension, String targetFileName, Arguments arguments) {
        return mainSubFolder;
    }

    @Override
    public String createDestinationFileNameWithoutExtension(PostfixDescriptor density, Dimension dimension, String targetFileName, Arguments arguments) {
        return targetFileName + density.postFix;
    }

    @Override
    public void onPreExecute(File dstFolder, String targetFileName, List<PostfixDescriptor> densityDescriptions, ImageType imageType, Arguments arguments) throws Exception {

    }

    @Override
    public void onPostExecute(Arguments arguments) {

    }

    @Override
    public void clean(Arguments arguments) {
        if (arguments.platform.size() == 1) {
            MiscUtil.deleteFolder(new File(arguments.dst, ROOT_FOLDER));
        } else {
            MiscUtil.deleteFolder(new File(new File(arguments.dst, WEB_FOLDER_NAME), ROOT_FOLDER));
        }
    }
}
