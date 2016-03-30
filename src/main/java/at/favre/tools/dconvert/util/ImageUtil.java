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

package at.favre.tools.dconvert.util;

import at.favre.tools.dconvert.arg.Arguments;
import at.favre.tools.dconvert.arg.ImageType;
import at.favre.tools.dconvert.converters.scaling.IBufferedImageScaler;
import at.favre.tools.dconvert.converters.scaling.ThumbnailatorScaler;
import com.twelvemonkeys.imageio.metadata.CompoundDirectory;
import com.twelvemonkeys.imageio.metadata.exif.EXIFReader;
import com.twelvemonkeys.imageio.metadata.jpeg.JPEG;
import com.twelvemonkeys.imageio.metadata.jpeg.JPEGSegment;
import com.twelvemonkeys.imageio.metadata.jpeg.JPEGSegmentUtil;

import javax.imageio.*;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Main Util class containing all
 */
public class ImageUtil {
	public static final ConvolveOp OP_ANTIALIAS = new ConvolveOp(
			new Kernel(3, 3, new float[]{.0f, .08f, .0f, .08f, .68f, .08f,
					.0f, .08f, .0f}), ConvolveOp.EDGE_NO_OP, null);

	public static LoadedImage loadImage(File input) throws Exception {
		if (input == null) {
			throw new IllegalArgumentException("input == null!");
		}
		if (!input.canRead()) {
			throw new IIOException("Can't read input file!");
		}

		ImageInputStream stream = ImageIO.createImageInputStream(input);
		if (stream == null) {
			throw new IIOException("Can't create an ImageInputStream!");
		}
		LoadedImage image = read(stream, Arguments.getImageType(input));
		if (image.getImage() == null) {
			stream.close();
		}
		return new LoadedImage(input, image.getImage(), image.getMetadata(), readExif(input));
	}

	private static CompoundDirectory readExif(File input) throws IOException {
		if (Arguments.getImageType(input) == ImageType.JPG) {
			try (ImageInputStream stream = ImageIO.createImageInputStream(input)) {
				List<JPEGSegment> exifSegment = JPEGSegmentUtil.readSegments(stream, JPEG.APP1, "Exif");
				if (!exifSegment.isEmpty()) {
					InputStream exifData = exifSegment.get(0).data();
					exifData.read(); // Skip 0-pad for Exif in JFIF
					try (ImageInputStream exifStream = ImageIO.createImageInputStream(exifData)) {
						return (CompoundDirectory) new EXIFReader().read(exifStream);
					}
				}
			} catch (Exception e) {
				System.err.println("could not read exif");
				e.printStackTrace();
				return null;
			}
		}
		return null;
	}

	private static LoadedImage read(ImageInputStream stream, ImageType imageType) throws IOException {
		if (stream == null) {
			throw new IllegalArgumentException("stream == null!");
		}

		Iterator iter = ImageIO.getImageReaders(stream);
		if (!iter.hasNext()) {
			return null;
		}

		ImageReader reader = (ImageReader) iter.next();
		ImageReadParam param = reader.getDefaultReadParam();
		reader.setInput(stream, true, true);
		BufferedImage bi;
		IIOMetadata metadata;
		try {
			metadata = reader.getImageMetadata(0);
			bi = reader.read(0, param);
		} finally {
			reader.dispose();
			stream.close();
		}

		return new LoadedImage(null, bi, metadata, null);
	}


	public static List<File> compressToFile(File targetFile, List<ImageType.ECompression> compressionList, LoadedImage imageData, Dimension targetDimension,
	                                        float compressionQuality, boolean skipIfExists, boolean antiAlias, boolean isNinePatch) throws Exception {
		List<File> files = new ArrayList<>(2);
		for (ImageType.ECompression compression : compressionList) {
			File imageFile = new File(targetFile.getAbsolutePath() + "." + compression.extension);

			if (imageFile.exists() && skipIfExists) {
				break;
			}

			BufferedImage scaledImage;
			if (isNinePatch && compression == ImageType.ECompression.PNG) {
				scaledImage = new NinePatchScaler().scale(imageData.getImage(), targetDimension);
			} else {
				scaledImage = getDefaultScaler().scale(imageData.getImage(), targetDimension.width, targetDimension.height, compression, Color.white, antiAlias);
			}

			if (compression == ImageType.ECompression.JPG) {
				compressJpeg(scaledImage, null, compressionQuality, imageFile);
			} else {
				ImageIO.write(scaledImage, compression.name().toLowerCase(), imageFile);
			}
			scaledImage.flush();
			files.add(imageFile);
		}
		return files;
	}

	public static void compressJpeg(BufferedImage bufferedImage, CompoundDirectory exif, float quality, File targetFile) throws IOException {
		ImageWriter jpgWriter = ImageIO.getImageWritersByFormatName("jpg").next();
		ImageWriteParam jpgWriteParam = jpgWriter.getDefaultWriteParam();
		jpgWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
		jpgWriteParam.setCompressionQuality(quality);

		ImageWriter writer = null;
		try (ImageOutputStream outputStream = new FileImageOutputStream(targetFile)) {
			//if (false && exif != null) {
			//  EXIFWriter exifWriter = new EXIFWriter();
			//List<Entry> entryList = new ArrayList<>();
			//for (int i = 0; i < exif.directoryCount(); i++) {
			//  for (Entry entry : exif.getDirectory(i)) {
			//    entryList.add(entry);
			//}
			//}
			//TODO: fix exif writing
			//exifWriter.write(exif, outputStream);
			//}
			writer = ImageIO.getImageWritersByFormatName("jpg").next();
			writer.setOutput(outputStream);
			writer.write(null, new IIOImage(bufferedImage, null, null), jpgWriteParam);
		} finally {
			if (writer != null) writer.dispose();
		}
	}

	public static IBufferedImageScaler getDefaultScaler() {
		return new ThumbnailatorScaler();
	}

	@Deprecated
	public static BufferedImage readSvg(File file, Dimension sourceDimension) throws Exception {
		try (ImageInputStream input = ImageIO.createImageInputStream(file)) {
			Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
			if (!readers.hasNext()) {
				throw new IllegalArgumentException("No reader for: " + file);
			}

			ImageReader reader = readers.next();
			try {
				reader.setInput(input);
				ImageReadParam param = reader.getDefaultReadParam();
				param.setSourceRenderSize(sourceDimension);
				return reader.read(0, param);
			} finally {
				reader.dispose();
			}
		}
	}

	/**
	 * Gets image dimensions for given file
	 *
	 * @param imgFile image file
	 * @return dimensions of image
	 * @throws IOException if the file is not a known image
	 */
	public static Dimension getImageDimension(File imgFile) throws IOException {
		int pos = imgFile.getName().lastIndexOf(".");
		if (pos == -1)
			throw new IOException("No extension for file: " + imgFile.getAbsolutePath());
		String suffix = imgFile.getName().substring(pos + 1);
		Iterator<ImageReader> iter = ImageIO.getImageReadersBySuffix(suffix);
		if (iter.hasNext()) {
			ImageReader reader = iter.next();
			try {
				ImageInputStream stream = new FileImageInputStream(imgFile);
				reader.setInput(stream);
				int width = reader.getWidth(reader.getMinIndex());
				int height = reader.getHeight(reader.getMinIndex());
				return new Dimension(width, height);
			} finally {
				reader.dispose();
			}
		}

		throw new IOException("Not a known image file: " + imgFile.getAbsolutePath());
	}
}
