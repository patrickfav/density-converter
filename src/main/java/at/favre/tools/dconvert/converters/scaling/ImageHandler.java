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

package at.favre.tools.dconvert.converters.scaling;

import at.favre.tools.dconvert.arg.Arguments;
import at.favre.tools.dconvert.arg.EScalingAlgorithm;
import at.favre.tools.dconvert.arg.ImageType;
import at.favre.tools.dconvert.util.LoadedImage;
import at.favre.tools.dconvert.util.MiscUtil;
import at.favre.tools.dconvert.util.NinePatchScaler;
import com.twelvemonkeys.imageio.metadata.CompoundDirectory;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Handles scaling and writing/compression images to disk
 */
public class ImageHandler {
    private static final Color DEFAULT_COLOR = Color.white;
    public static final boolean TEST_MODE = false;
    public static final ConvolveOp OP_ANTIALIAS = new ConvolveOp(new Kernel(3, 3, new float[]{.0f, .08f, .0f, .08f, .68f, .08f, .0f, .08f, .0f}), ConvolveOp.EDGE_NO_OP, null);
    public static final Map<ScaleAlgorithm, Long> traceMap = new HashMap<>();
    private final Arguments args;

    public ImageHandler(Arguments args) {
        this.args = args;
    }

    public List<File> saveToFile(File targetFile, LoadedImage imageData, Dimension targetDimension, boolean isNinePatch) throws Exception {

        List<File> files = new ArrayList<>(2);
        List<ImageType.ECompression> compressionList = Arguments.getOutCompressionForType(args.outputCompressionMode, Arguments.getImageType(imageData.getSourceFile()));
        for (ImageType.ECompression compression : compressionList) {
            File imageFile = new File(targetFile.getAbsolutePath() + "." + compression.extension);

            if (imageFile.exists() && args.skipExistingFiles) {
                break;
            }

            List<ScaleAlgorithm> algorithms = getScaleAlgorithm(getScalingAlgorithm(getScalingType(imageData, targetDimension)), getScalingType(imageData, targetDimension));

            for (ScaleAlgorithm scaleAlgorithm : algorithms) {

                if (!traceMap.containsKey(scaleAlgorithm)) {
                    traceMap.put(scaleAlgorithm, 0L);
                }

                BufferedImage scaledImage;
                if (isNinePatch && compression == ImageType.ECompression.PNG) {
                    scaledImage = new NinePatchScaler().scale(imageData.getImage(), targetDimension, getAsScalingAlgorithm(scaleAlgorithm, compression));
                } else {
                    long startNanos = System.nanoTime();
                    scaledImage = scale(scaleAlgorithm, imageData.getImage(), targetDimension.width, targetDimension.height, compression, DEFAULT_COLOR);
                    traceMap.put(scaleAlgorithm, traceMap.get(scaleAlgorithm) + (System.nanoTime() - startNanos));
                }

                File fileToSave = imageFile;

                if (algorithms.size() > 1) {
                    fileToSave = new File(imageFile.getParentFile(), MiscUtil.getFileNameWithoutExtension(imageFile) + "." + scaleAlgorithm.toString() + "." + MiscUtil.getFileExtension(imageFile));
                }

                if (compression == ImageType.ECompression.JPG) {
                    compressJpeg(scaledImage, null, args.compressionQuality, fileToSave);
                } else {
                    ImageIO.write(scaledImage, compression.name().toLowerCase(), fileToSave);
                }
                scaledImage.flush();
                files.add(imageFile);
            }
        }
        return files;
    }

    private void compressJpeg(BufferedImage bufferedImage, CompoundDirectory exif, float quality, File targetFile) throws IOException {
        ImageWriter jpgWriter = ImageIO.getImageWritersByFormatName("jpg").next();
        ImageWriteParam jpgWriteParam = jpgWriter.getDefaultWriteParam();
        jpgWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        jpgWriteParam.setCompressionQuality(quality);

        ImageWriter writer = null;
        try (ImageOutputStream outputStream = new FileImageOutputStream(targetFile)) {
            writer = ImageIO.getImageWritersByFormatName("jpg").next();
            writer.setOutput(outputStream);
            writer.write(null, new IIOImage(bufferedImage, null, null), jpgWriteParam);
        } finally {
            if (writer != null) writer.dispose();
        }
    }

    private EScalingAlgorithm getScalingAlgorithm(EScalingAlgorithm.Type type) {
        return type == EScalingAlgorithm.Type.UPSCALING ? args.upScalingAlgorithm : args.downScalingAlgorithm;
    }

    private EScalingAlgorithm.Type getScalingType(LoadedImage imageData, Dimension targetDimension) {
        long targetSize = targetDimension.height * targetDimension.width;
        long sourceSize = imageData.getImage().getHeight() * imageData.getImage().getWidth();
        return targetSize >= sourceSize ? EScalingAlgorithm.Type.UPSCALING : EScalingAlgorithm.Type.DOWNSCALING;
    }

    private List<ScaleAlgorithm> getScaleAlgorithm(EScalingAlgorithm algorithm, EScalingAlgorithm.Type type) {
        if (TEST_MODE) {
            return EScalingAlgorithm.getAllEnabled().stream().filter(eScalingAlgorithm -> eScalingAlgorithm.getSupportedForType().contains(type)).map(EScalingAlgorithm::getImplementation).collect(Collectors.toList());
        } else {
            return Collections.singletonList(algorithm.getImplementation());
        }
    }

    private BufferedImage scale(ScaleAlgorithm scaleAlgorithm, BufferedImage imageToScale, int dWidth, int dHeight, ImageType.ECompression compression, Color background) {

        BufferedImage scaledImage;

        if (dWidth == imageToScale.getWidth() && dHeight == imageToScale.getHeight()) {
            scaledImage = imageToScale;
        } else {
            scaledImage = scaleAlgorithm.scale(imageToScale, dWidth, dHeight);
        }

        if (!compression.hasTransparency) {
            BufferedImage convertedImg = new BufferedImage(scaledImage.getWidth(), scaledImage.getHeight(), BufferedImage.TYPE_INT_RGB);
            convertedImg.getGraphics().drawImage(scaledImage, 0, 0, background, null);
            scaledImage = convertedImg;
        }

        if (args.enableAntiAliasing) {
            scaledImage = OP_ANTIALIAS.filter(scaledImage, null);
        }

        return scaledImage;
    }

    private ScaleAlgorithm getAsScalingAlgorithm(final ScaleAlgorithm algorithm, ImageType.ECompression compression) {
        return (imageToScale, dWidth, dHeight) -> ImageHandler.this.scale(algorithm, imageToScale, dWidth, dHeight, compression, DEFAULT_COLOR);
    }
}
