package at.favre.tools.converter.platforms;

import at.favre.tools.converter.ConverterUtil;
import at.favre.tools.converter.arg.Arguments;
import at.favre.tools.converter.arg.ECompression;
import at.favre.tools.converter.arg.EPlatform;
import at.favre.tools.converter.platforms.descriptors.AndroidDensityDescriptor;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Converts and creates Android-style resource set
 */
public class AndroidConverter extends APlatformConverter<AndroidDensityDescriptor> {

	@Override
	public List<AndroidDensityDescriptor> usedOutputDensities(Arguments arguments) {
		List<AndroidDensityDescriptor> list = new ArrayList<>();
		if (arguments.includeObsoleteFormats) {
			list.add(new AndroidDensityDescriptor(0.75f, "ldpi", "drawable-ldpi"));
		}
		list.add(new AndroidDensityDescriptor(1, "mdpi", "drawable-mdpi"));
		list.add(new AndroidDensityDescriptor(1.5f, "hdpi", "drawable-mdpi"));
		list.add(new AndroidDensityDescriptor(2, "xhdpi", "drawable-xhdpi"));
		list.add(new AndroidDensityDescriptor(3, "xxhdpi", "drawable-xxhdpi"));
		list.add(new AndroidDensityDescriptor(4, "xxxhdpi", "drawable-xxxhdpi"));
		return list;
	}

	@Override
	public String getConverterName() {
		return "android-converter";
	}

	@Override
	public File createMainSubFolder(File destinationFolder, String targetImageFileName, Arguments arguments) {
		if (arguments.platform != EPlatform.ANROID) {
			return ConverterUtil.createAndCheckFolder(new File(destinationFolder, "android").getAbsolutePath());
		} else {
			return destinationFolder;
		}
	}

	@Override
	public File createFolderForOutputFile(File mainSubFolder, AndroidDensityDescriptor density, Dimension dimension, String targetFileName, Arguments arguments) {
		return ConverterUtil.createAndCheckFolder(new File(mainSubFolder, density.folderName).getAbsolutePath());
	}

	@Override
	public String createDestinationFileNameWithoutExtension(AndroidDensityDescriptor density, Dimension dimension, String targetFileName, Arguments arguments) {
		return targetFileName;
	}

	@Override
	public void onPreExecute(File dstFolder, String targetFileName, List<AndroidDensityDescriptor> densityDescriptions, ECompression srcCompression, Arguments arguments) throws Exception {
		//nothing
	}

	@Override
	public void onPostExecute(Arguments arguments) {
		//nothing
	}
}