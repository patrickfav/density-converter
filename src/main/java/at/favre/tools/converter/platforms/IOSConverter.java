package at.favre.tools.converter.platforms;

import at.favre.tools.converter.Arguments;
import at.favre.tools.converter.ConverterUtil;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Needed info to convert for Android
 */
public class IOSConverter extends APlatformConverter<IOSDensityDescription> {

	@Override
	public List<IOSDensityDescription> usedOutputDensities(Arguments arguments) {
		List<IOSDensityDescription> list = new ArrayList<>();
		list.add(new IOSDensityDescription(1, "1x", ""));
		list.add(new IOSDensityDescription(2, "2x", "_2x"));
		list.add(new IOSDensityDescription(3, "3x", "_3x"));
		return list;
	}

	@Override
	public String getConverterName() {
		return "ios-converter";
	}

	@Override
	public File createMainSubFolder(File destinationFolder, String targetImageFileName, Arguments arguments) {
		if (arguments.platform != Arguments.Platform.IOS) {
			destinationFolder = ConverterUtil.createAndCheckFolder(new File(destinationFolder, "ios").getAbsolutePath());
		}
		return ConverterUtil.createAndCheckFolder(new File(destinationFolder, targetImageFileName + ".imageset").getAbsolutePath());
	}

	@Override
	public File createFolderForOutputFile(File mainSubFolder, IOSDensityDescription density, Dimension dimension, String targetFileName, Arguments arguments) {
		return mainSubFolder;
	}

	@Override
	public String createDestinationFileNameWithoutExtension(IOSDensityDescription density, Dimension dimension, String targetFileName, Arguments arguments) {
		return targetFileName + density.postFix;
	}

	@Override
	public void onPreExecute(File dstFolder, String targetFileName, List<IOSDensityDescription> densityDescriptions, Arguments arguments) throws Exception {
		writeContentJson(dstFolder, targetFileName, densityDescriptions, arguments.outputCompressionMode);
	}

	@Override
	public void onPostExecute(Arguments arguments) {

	}

	private void writeContentJson(File dstFolder, String targetFileName, List<IOSDensityDescription> iosDensityDescriptions, Arguments.OutputCompressionMode outputCompressionMode) throws IOException {
		File contentJson = new File(dstFolder, "Content.json");

		if (contentJson.exists()) {
			contentJson.delete();
		}
		contentJson.createNewFile();

		try (PrintWriter out = new PrintWriter(contentJson)) {
			out.println(createContentJson(targetFileName, iosDensityDescriptions, outputCompressionMode));
		}
	}

	private String createContentJson(String targetFileName, List<IOSDensityDescription> iosDensityDescriptions, Arguments.OutputCompressionMode outputCompressionMode) {
		StringBuilder sb = new StringBuilder("{\n\t\"images\": [");

		for (IOSDensityDescription densityDescription : iosDensityDescriptions) {
			sb.append("\n\t\t{\n" +
					"\t\t\t\"filename\": \"" + targetFileName + densityDescription.postFix + "." + outputCompressionMode.name().toLowerCase() + "\",\n" +
					"\t\t\t\"idiom\": \"universal\",\n" +
					"\t\t\t\"scrScale\": \"" + densityDescription.name + "\"\n" +
					"\t\t},");
		}
		sb.setLength(sb.length() - 1);
		sb.append("\t],\n\t\"info\": {\n\t\t\"author\": \"xcode\",\n\t\t\"version\": 1\n\t}\n}");

		return sb.toString();
	}
}