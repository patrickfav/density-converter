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

package at.favre.tools.dconvert.converters;

import at.favre.tools.dconvert.arg.Arguments;
import at.favre.tools.dconvert.arg.ImageType;
import at.favre.tools.dconvert.converters.descriptors.PostfixDescriptor;
import at.favre.tools.dconvert.util.MiscUtil;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Needed info to convert for Android
 */
public class IOSConverter extends APlatformConverter<PostfixDescriptor> {
	public static final String ROOT_FOLDER = "AssetCatalog";

	@Override
	public List<PostfixDescriptor> usedOutputDensities(Arguments arguments) {
		return getIosDescriptors();
	}

	public static List<PostfixDescriptor> getIosDescriptors() {
		List<PostfixDescriptor> list = new ArrayList<>();
		list.add(new PostfixDescriptor(1, "1x", ""));
		list.add(new PostfixDescriptor(2, "2x", "@2x"));
		list.add(new PostfixDescriptor(3, "3x", "@3x"));
		return list;
	}

	@Override
	public String getConverterName() {
		return "ios-converter";
	}

	@Override
	public File createMainSubFolder(File destinationFolder, String targetImageFileName, Arguments arguments) {
		if (arguments.platform.size() > 1) {
			destinationFolder = MiscUtil.createAndCheckFolder(new File(destinationFolder, "ios").getAbsolutePath(), arguments.dryRun);
		}
		if (arguments.iosCreateImagesetFolders) {
			return MiscUtil.createAndCheckFolder(new File(destinationFolder, targetImageFileName + ".imageset").getAbsolutePath(), arguments.dryRun);
		} else {
			return MiscUtil.createAndCheckFolder(new File(destinationFolder, ROOT_FOLDER).getAbsolutePath(), arguments.dryRun);
		}
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
		if (!arguments.dryRun && arguments.iosCreateImagesetFolders) {
			writeContentJson(dstFolder, targetFileName, densityDescriptions, Arguments.getOutCompressionForType(arguments.outputCompressionMode, imageType));
		}
	}

	@Override
	public void onPostExecute(Arguments arguments) {

	}

	private void writeContentJson(File dstFolder, String targetFileName, List<PostfixDescriptor> iosDensityDescriptions, List<ImageType.ECompression> compressions) throws IOException {
		File contentJson = new File(dstFolder, "Content.json");

		if (contentJson.exists()) {
			contentJson.delete();
		}
		contentJson.createNewFile();

		try (PrintWriter out = new PrintWriter(contentJson)) {
			out.println(createContentJson(targetFileName, iosDensityDescriptions, compressions));
		}
	}

	private String createContentJson(String targetFileName, List<PostfixDescriptor> iosDensityDescriptions, List<ImageType.ECompression> compressions) {
		StringBuilder sb = new StringBuilder("{\n\t\"images\": [");
		for (ImageType.ECompression compression : compressions) {
			for (PostfixDescriptor densityDescription : iosDensityDescriptions) {
				sb.append("\n\t\t{\n" +
						"\t\t\t\"filename\": \"" + targetFileName + densityDescription.postFix + "." + compression.name().toLowerCase() + "\",\n" +
						"\t\t\t\"idiom\": \"universal\",\n" +
						"\t\t\t\"scrScale\": \"" + densityDescription.name + "\"\n" +
						"\t\t},");
			}
		}
		sb.setLength(sb.length() - 1);
		sb.append("\n\t],\n\t\"info\": {\n\t\t\"author\": \"xcode\",\n\t\t\"version\": 1\n\t}\n}");

		return sb.toString();
	}
}