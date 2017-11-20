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

package at.favre.tools.dconvert.test;

import at.favre.tools.dconvert.arg.Arguments;
import at.favre.tools.dconvert.arg.EOutputCompressionMode;
import at.favre.tools.dconvert.arg.EPlatform;
import at.favre.tools.dconvert.converters.IOSConverter;
import at.favre.tools.dconvert.converters.descriptors.PostfixDescriptor;
import at.favre.tools.dconvert.util.ImageUtil;
import at.favre.tools.dconvert.util.MiscUtil;
import org.junit.Test;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit test of the {@link at.favre.tools.dconvert.converters.IPlatformConverter} for ios
 */
public class IOSConverterTest extends AConverterTest {
    @Override
    protected EPlatform getType() {
        return EPlatform.IOS;
    }

    @Override
    protected void checkOutDir(File dstDir, Arguments arguments, List<File> files, EPlatform type) throws IOException {
        checkOutDirIos(dstDir, arguments, files);
    }

    @Test
    public void testMultiplePngImagesetFolders() throws Exception {
        List<File> files = copyToTestPath(defaultSrc, "png_example1_alpha_144.png", "png_example2_alpha_144.png", "jpg_example2_512.jpg");
        test(new Arguments.Builder(defaultSrc, DEFAULT_SCALE).compression(EOutputCompressionMode.SAME_AS_INPUT_PREF_PNG, 0.5f)
                .dstFolder(defaultDst).platform(Collections.singleton(getType())).iosCreateImagesetFolders(true).build(), files);
    }

    @Test
    public void testSinglePngImagesetFolder() throws Exception {
        List<File> files = copyToTestPath(defaultSrc, "png_example1_alpha_144.png");
        test(new Arguments.Builder(defaultSrc, DEFAULT_SCALE).compression(EOutputCompressionMode.SAME_AS_INPUT_PREF_PNG, 0.5f)
                .dstFolder(defaultDst).platform(Collections.singleton(getType())).iosCreateImagesetFolders(true).build(), files);
    }

    public static void checkOutDirIos(File dstDir, Arguments arguments, List<File> files) throws IOException {
        Map<File, Dimension> dimensionMap = createDimensionMap(files);

        List<PostfixDescriptor> densityDescriptors = IOSConverter.getIosDescriptors();

        System.out.println("ios-convert " + files);

        if (arguments.iosCreateImagesetFolders) {
            checkWithImagesetFolders(dstDir, arguments, files, dimensionMap, densityDescriptors);
        } else {
            checkOutDirPostfixDescr(new File(dstDir, IOSConverter.ROOT_FOLDER), arguments, files, densityDescriptors);
        }
    }

    private static void checkWithImagesetFolders(File dstDir, Arguments arguments, List<File> files, Map<File, Dimension> dimensionMap, List<PostfixDescriptor> densityDescriptors) throws IOException {
        assertTrue("src files and dst folder count should match", files.size() == dstDir.listFiles().length);
        for (File iosImgFolder : dstDir.listFiles()) {
            boolean found = false;
            File srcFile = null;
            for (File file : files) {
                if (String.valueOf(MiscUtil.getFileNameWithoutExtension(file) + ".imageset").equals(iosImgFolder.getName())) {
                    found = true;
                    srcFile = file;
                    break;
                }
            }

            assertTrue("root image folder should be found ", found);
            assertTrue("image folder should contain at least 1 file", iosImgFolder.listFiles().length > 0);

            List<ImageInfo> expectedFiles = new ArrayList<>();
            for (PostfixDescriptor densityDescriptor : densityDescriptors) {
                final File finalSrcFile = srcFile;
                expectedFiles.addAll(Arguments.getOutCompressionForType(
                        arguments.outputCompressionMode, Arguments.getImageType(srcFile)).stream().map(compression ->
                        new ImageInfo(finalSrcFile, MiscUtil.getFileNameWithoutExtension(finalSrcFile) + densityDescriptor.postFix + "." + compression.extension, densityDescriptor.scale)).collect(Collectors.toList()));
            }

            for (File dstImageFile : iosImgFolder.listFiles()) {
                for (ImageInfo expectedFile : expectedFiles) {
                    if (dstImageFile.getName().equals(expectedFile.targetFileName)) {
                        expectedFile.found = true;

                        Dimension expectedDimension = getScaledDimension(expectedFile.srcFile, arguments, dimensionMap.get(expectedFile.srcFile), expectedFile.scale, false);
                        assertEquals("dimensions should match", expectedDimension, ImageUtil.getImageDimension(dstImageFile));
                    }
                }
            }

            for (ImageInfo expectedFile : expectedFiles) {
                assertTrue(expectedFile.targetFileName + " expected in folder " + srcFile, expectedFile.found);
            }

            System.out.print("found " + expectedFiles.size() + " files in " + iosImgFolder + ", ");
        }
        System.out.println();
    }
}
