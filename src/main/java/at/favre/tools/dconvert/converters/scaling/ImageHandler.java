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
import java.awt.color.ColorSpace;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles scaling and writing/compression images to disk
 */
public class ImageHandler {
    private static final Color DEFAULT_COLOR = Color.white;
    public static final boolean TEST_MODE = true;
    public static final ConvolveOp OP_ANTIALIAS = new ConvolveOp(new Kernel(3, 3, new float[]{.0f, .08f, .0f, .08f, .68f, .08f, .0f, .08f, .0f}), ConvolveOp.EDGE_NO_OP, null);
    public static Map<ScaleAlgorithm, Long> traceMap = new HashMap<>();
    private Arguments args;

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
            BufferedImage copy = deepCopy(imageToScale);
//            BufferedImage bufferedImage = GraphicsUtil.makeLinearBufferedImage(imageToScale.getWidth(),imageToScale.getHeight(),imageToScale.getColorModel().isAlphaPremultiplied());
//            bufferedImage.getGraphics().drawImage(imageToScale,0,0,background,null);
//
//            for (int i = 0; i < imageToScale.getWidth(); i++) {
//                for (int j = 0; j < imageToScale.getHeight(); j++) {
//                    imageToScale.getRaster().getDataBuffer().ge;
//
//                }
//            }
            ColorModel ccm = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_LINEAR_RGB), true, false, Transparency.TRANSLUCENT, DataBuffer.TYPE_BYTE);
            copy = new ColorConvertOp(imageToScale.getColorModel().getColorSpace(),ccm.getColorSpace(), null).filter(copy, null);

            scaledImage = scaleAlgorithm.scale(copy, dWidth, dHeight);

            scaledImage = new ColorConvertOp(scaledImage.getColorModel().getColorSpace(),ColorSpace.getInstance(ColorSpace.CS_sRGB), null).filter(scaledImage, null);
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

    static BufferedImage deepCopy(BufferedImage bi) {
        ColorModel cm = bi.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = bi.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }

    private static int[][] convertTo2DWithoutUsingGetRGB(BufferedImage image) {

        final byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        final int width = image.getWidth();
        final int height = image.getHeight();
        final boolean hasAlphaChannel = image.getAlphaRaster() != null;

        int[][] result = new int[height][width];
        if (hasAlphaChannel) {
            final int pixelLength = 4;
            for (int pixel = 0, row = 0, col = 0; pixel < pixels.length; pixel += pixelLength) {
                int argb = 0;
                argb += (((int) pixels[pixel] & 0xff) << 24); // alpha
                argb += ((int) pixels[pixel + 1] & 0xff); // blue
                argb += (((int) pixels[pixel + 2] & 0xff) << 8); // green
                argb += (((int) pixels[pixel + 3] & 0xff) << 16); // red
                result[row][col] = argb;
                col++;
                if (col == width) {
                    col = 0;
                    row++;
                }
            }
        } else {
            final int pixelLength = 3;
            for (int pixel = 0, row = 0, col = 0; pixel < pixels.length; pixel += pixelLength) {
                int argb = 0;
                argb += -16777216; // 255 alpha
                argb += ((int) pixels[pixel] & 0xff); // blue
                argb += (((int) pixels[pixel + 1] & 0xff) << 8); // green
                argb += (((int) pixels[pixel + 2] & 0xff) << 16); // red
                result[row][col] = argb;
                col++;
                if (col == width) {
                    col = 0;
                    row++;
                }
            }
        }

        return result;
    }

//    public float[] fromsRGB(int argb) {
//        int alpha = (argb >> 24) & 0x000000FF;
//        int red = (argb >> 16) & 0x000000FF;
//        int green = (argb >>8 ) & 0x000000FF;
//        int blue = (argb) & 0x000000FF;
//
//        Color.getColor("",argb);
//        float[] out = new float[3];
//
//        // Convert non-linear RGB coordinates to linear ones,
//        //  numbers from the w3 spec.
//        for (int i = 0; i < 3; i++) {
//            float n = in[i];
//            if (n < 0)
//                n = 0f;
//            if (n > 1)
//                n = 1f;
//            if (n <= 0.03928f)
//                out[i] = (float) (n / 12.92);
//            else
//                out[i] = (float) (Math.exp(2.4 * Math.log((n + 0.055) / 1.055)));
//        }
//        return out;
//    }
}
