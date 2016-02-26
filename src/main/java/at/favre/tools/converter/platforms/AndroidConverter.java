package at.favre.tools.converter.platforms;

import at.favre.tools.converter.Arguments;
import at.favre.tools.converter.ConverterUtil;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Converts and creates Android-style resource set
 */
public class AndroidConverter extends APlatformConverter<AndroidDensityDescription> {

	@Override
	public List<AndroidDensityDescription> usedOutputDensities(Arguments arguments) {
		List<AndroidDensityDescription> list = new ArrayList<>();
		if (arguments.includeObsoleteFormats) {
			list.add(new AndroidDensityDescription(0.75f, "ldpi", "drawable-ldpi"));
		}
		list.add(new AndroidDensityDescription(1, "mdpi", "drawable-mdpi"));
		list.add(new AndroidDensityDescription(1.5f, "hdpi", "drawable-mdpi"));
		list.add(new AndroidDensityDescription(2, "xhdpi", "drawable-xhdpi"));
		list.add(new AndroidDensityDescription(3, "xxhdpi", "drawable-xxhdpi"));
		list.add(new AndroidDensityDescription(4, "xxxhdpi", "drawable-xxxhdpi"));
		return list;
	}

	@Override
	public String getConverterName() {
		return "android-converter";
	}

	@Override
	public File createMainSubFolder(File destinationFolder, String targetImageFileName, Arguments arguments) {
		if (arguments.platform != Arguments.Platform.ANROID) {
			return ConverterUtil.createAndCheckFolder(new File(destinationFolder, "android").getAbsolutePath());
		} else {
			return destinationFolder;
		}
	}

	@Override
	public File createFolderForOutputFile(File mainSubFolder, AndroidDensityDescription density, Dimension dimension, String targetFileName, Arguments arguments) {
		return ConverterUtil.createAndCheckFolder(new File(mainSubFolder, density.folderName).getAbsolutePath());
	}

	@Override
	public String createDestinationFileNameWithoutExtension(AndroidDensityDescription density, Dimension dimension, String targetFileName, Arguments arguments) {
		return targetFileName;
	}

	@Override
	public void onPreExecute(File dstFolder, String targetFileName, List<AndroidDensityDescription> densityDescriptions, Arguments arguments) throws Exception {
		//nothing
	}

	@Override
	public void onPostExecute(Arguments arguments) {
		//nothing
	}
}