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
 * Needed info to convert for Windows
 */
public class WindowsConverter extends APlatformConverter<PostfixDescriptor> {
    private static final String WINDOWS_FOLDER_NAME = "windows";
    public static final String ROOT_FOLDER = "Assets";

    @Override
    public List<PostfixDescriptor> usedOutputDensities(Arguments arguments) {
        return getWindowsDescriptors();
    }

    public static List<PostfixDescriptor> getWindowsDescriptors() {
        List<PostfixDescriptor> list = new ArrayList<>();
        list.add(new PostfixDescriptor(1, "100%", ".scale-100"));
        list.add(new PostfixDescriptor(1.4f, "140%", ".scale-140"));
        list.add(new PostfixDescriptor(1.8f, "180%", ".scale-180"));
        list.add(new PostfixDescriptor(2.4f, "240%", ".scale-240"));
        return list;
    }

    @Override
    public String getConverterName() {
        return "windows-converter";
    }

    @Override
    public File createMainSubFolder(File destinationFolder, String targetImageFileName, Arguments arguments) {
        if (arguments.platform.size() > 1) {
            destinationFolder = MiscUtil.createAndCheckFolder(new File(destinationFolder, WINDOWS_FOLDER_NAME).getAbsolutePath(), arguments.dryRun);
        }
        return MiscUtil.createAndCheckFolder(new File(destinationFolder, WindowsConverter.ROOT_FOLDER).getAbsolutePath(), arguments.dryRun);
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
            MiscUtil.deleteFolder(new File(new File(arguments.dst, WINDOWS_FOLDER_NAME), ROOT_FOLDER));
        }
    }
}
