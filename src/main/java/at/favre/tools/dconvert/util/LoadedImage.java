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

package at.favre.tools.dconvert.util;

import com.twelvemonkeys.imageio.metadata.CompoundDirectory;

import javax.imageio.metadata.IIOMetadata;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Wraps a {@link java.awt.image.BufferedImage} and some other meta data
 */

public class LoadedImage {
    private final File sourceFile;
    private final BufferedImage image;
    private final IIOMetadata metadata;
    private final CompoundDirectory directory;

    public LoadedImage(File sourceFile, BufferedImage image, IIOMetadata metadata, CompoundDirectory directory) {
        this.sourceFile = sourceFile;
        this.image = image;
        this.metadata = metadata;
        this.directory = directory;
    }

    public File getSourceFile() {
        return sourceFile;
    }

    public BufferedImage getImage() {
        return image;
    }

    public IIOMetadata getMetadata() {
        return metadata;
    }

    public CompoundDirectory getExif() {
        return directory;
    }
}
