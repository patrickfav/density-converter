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
import at.favre.tools.dconvert.arg.EPlatform;
import at.favre.tools.dconvert.converters.WindowsConverter;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Unit test of the {@link at.favre.tools.dconvert.converters.IPlatformConverter} for windows
 */
public class WindowsConverterTest extends AConverterTest {
    @Override
    protected EPlatform getType() {
        return EPlatform.WINDOWS;
    }

    @Override
    protected void checkOutDir(File dstDir, Arguments arguments, List<File> files, EPlatform type) throws IOException {
        checkOutDirWindows(dstDir, arguments, files);
    }

    public static void checkOutDirWindows(File dstDir, Arguments arguments, List<File> files) throws IOException {
        System.out.println("windows-convert " + files);
        checkOutDirPostfixDescr(new File(dstDir, WindowsConverter.ROOT_FOLDER), arguments, files, WindowsConverter.getWindowsDescriptors());
    }
}
