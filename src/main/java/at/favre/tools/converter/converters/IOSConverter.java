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

package at.favre.tools.converter.converters;

import at.favre.tools.converter.ConverterUtil;
import at.favre.tools.converter.arg.Arguments;
import at.favre.tools.converter.arg.ECompression;
import at.favre.tools.converter.arg.EPlatform;
import at.favre.tools.converter.converters.descriptors.IOSDensityDescriptor;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Needed info to convert for Android
 */
public class IOSConverter extends APlatformConverter<IOSDensityDescriptor> {

	@Override
	public List<IOSDensityDescriptor> usedOutputDensities(Arguments arguments) {
		List<IOSDensityDescriptor> list = new ArrayList<>();
		list.add(new IOSDensityDescriptor(1, "1x", ""));
		list.add(new IOSDensityDescriptor(2, "2x", "_2x"));
		list.add(new IOSDensityDescriptor(3, "3x", "_3x"));
		return list;
	}

	@Override
	public String getConverterName() {
		return "ios-converter";
	}

	@Override
	public File createMainSubFolder(File destinationFolder, String targetImageFileName, Arguments arguments) {
		if (arguments.platform != EPlatform.IOS) {
			destinationFolder = ConverterUtil.createAndCheckFolder(new File(destinationFolder, "ios").getAbsolutePath());
		}
		return ConverterUtil.createAndCheckFolder(new File(destinationFolder, targetImageFileName + ".imageset").getAbsolutePath());
	}

	@Override
	public File createFolderForOutputFile(File mainSubFolder, IOSDensityDescriptor density, Dimension dimension, String targetFileName, Arguments arguments) {
		return mainSubFolder;
	}

	@Override
	public String createDestinationFileNameWithoutExtension(IOSDensityDescriptor density, Dimension dimension, String targetFileName, Arguments arguments) {
		return targetFileName + density.postFix;
	}

	@Override
	public void onPreExecute(File dstFolder, String targetFileName, List<IOSDensityDescriptor> densityDescriptions, ECompression srcCompression, Arguments arguments) throws Exception {
		writeContentJson(dstFolder, targetFileName, densityDescriptions, Arguments.getCompressionForType(arguments.outputCompressionMode, srcCompression));
	}

	@Override
	public void onPostExecute(Arguments arguments) {

	}

	private void writeContentJson(File dstFolder, String targetFileName, List<IOSDensityDescriptor> iosDensityDescriptions, List<ECompression> compressions) throws IOException {
		File contentJson = new File(dstFolder, "Content.json");

		if (contentJson.exists()) {
			contentJson.delete();
		}
		contentJson.createNewFile();

		try (PrintWriter out = new PrintWriter(contentJson)) {
			out.println(createContentJson(targetFileName, iosDensityDescriptions, compressions));
		}
	}

	private String createContentJson(String targetFileName, List<IOSDensityDescriptor> iosDensityDescriptions, List<ECompression> compressions) {
		StringBuilder sb = new StringBuilder("{\n\t\"images\": [");
		for (ECompression compression : compressions) {
			for (IOSDensityDescriptor densityDescription : iosDensityDescriptions) {
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