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
