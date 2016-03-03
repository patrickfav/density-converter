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

package at.favre.tools.converter.ui;

import at.favre.tools.converter.arg.Arguments;
import at.favre.tools.converter.arg.EOutputCompressionMode;
import at.favre.tools.converter.arg.EPlatform;
import at.favre.tools.converter.arg.RoundingHandler;
import org.apache.commons.cli.*;

import java.io.File;

/**
 * Handles parsing of command line arguments
 */
public class CLInterpreter {
	public static final String COMPRESSION_QUALITY_ARG = "compressionQuality";
	public static final String THREADS_ARG = "threads";
	private final static String SOURCE_ARG = "src";
	private final static String SCALE_ARG = "scale";
	private final static String PLATFORM_ARG = "platform";
	public static final String OUT_COMPRESSION_ARG = "outCompression";
	public static final String ROUNDING_MODE_ARG = "roundingMode";
	public static final String DST_ARG = "dst";
	public static final String VERBOSE_ARG = "verbose";
	public static final String SKIP_EXISTING_ARG = "skipExisting";

	public static Arguments parse(String[] args) {
		Options options = setupOptions();
		CommandLineParser parser = new DefaultParser();

		Arguments.Builder builder;
		try {
			CommandLine commandLine = parser.parse(options, args);

			if (commandLine.hasOption("gui")) {
				return Arguments.START_GUI;
			}

			if (commandLine.hasOption("h") || commandLine.hasOption("help")) {
				printHelp(options);
				return null;
			}

			if (commandLine.hasOption("v") || commandLine.hasOption("version")) {
				System.out.println("Version: " + CLInterpreter.class.getPackage().getImplementationVersion());
				return null;
			}

			builder = new Arguments.Builder(new File(commandLine.getOptionValue(SOURCE_ARG)), Float.valueOf(commandLine.getOptionValue(SCALE_ARG, "3")));

			if (commandLine.hasOption(DST_ARG)) {
				builder.dstFolder(new File(commandLine.getOptionValue(DST_ARG)));
			}

			float compressionQuality = Arguments.DEFAULT_COMPRESSION_QUALITY;
			if (commandLine.hasOption(COMPRESSION_QUALITY_ARG)) {
				compressionQuality = Float.valueOf(commandLine.getOptionValue(COMPRESSION_QUALITY_ARG));
			}

			if (commandLine.hasOption(OUT_COMPRESSION_ARG)) {
				switch (commandLine.getOptionValue(OUT_COMPRESSION_ARG)) {
					case "png":
						builder.compression(EOutputCompressionMode.PNG);
						break;
					case "jpg":
						builder.compression(EOutputCompressionMode.JPG, compressionQuality);
						break;
					case "gif":
						builder.compression(EOutputCompressionMode.GIF);
						break;
					case "png+jpg":
						builder.compression(EOutputCompressionMode.JPG_AND_PNG, compressionQuality);
						break;
					default:
						System.err.println("unknown compression type: " + commandLine.getOptionValue(OUT_COMPRESSION_ARG));
				}
			}

			if (commandLine.hasOption(PLATFORM_ARG)) {
				switch (commandLine.getOptionValue(PLATFORM_ARG)) {
					case "all":
						builder.platform(EPlatform.ALL);
						break;
					case "android":
						builder.platform(EPlatform.ANROID);
						break;
					case "ios":
						builder.platform(EPlatform.IOS);
						break;
					default:
						System.err.println("unknown mode: " + commandLine.getOptionValue(PLATFORM_ARG));
				}
			}

			if (commandLine.hasOption(ROUNDING_MODE_ARG)) {
				switch (commandLine.getOptionValue(ROUNDING_MODE_ARG)) {
					case "round":
						builder.scaleRoundingStragy(RoundingHandler.Strategy.ROUND_HALF_UP);
						break;
					case "ceil":
						builder.scaleRoundingStragy(RoundingHandler.Strategy.CEIL);
						break;
					case "floor":
						builder.scaleRoundingStragy(RoundingHandler.Strategy.FLOOR);
						break;
					default:
						System.err.println("unknown mode: " + commandLine.getOptionValue(ROUNDING_MODE_ARG));
				}
			}

			if (commandLine.hasOption(THREADS_ARG)) {
				builder.threadCount(Integer.valueOf(commandLine.getOptionValue(THREADS_ARG)));
			}

			builder.skipUpscaling(commandLine.hasOption("skipUpscaling"));
			builder.skipExistingFiles(commandLine.hasOption(SKIP_EXISTING_ARG));
			builder.includeAndroidLdpiTvdpi(commandLine.hasOption("androidIncludeLdpiTvdpi"));
			builder.verboseLog(commandLine.hasOption(VERBOSE_ARG));
			builder.haltOnError(commandLine.hasOption("haltOnError"));
			builder.createMipMapInsteadOfDrawableDir(commandLine.hasOption("androidMipmapInsteadOfDrawable"));
			builder.antiAliasing(commandLine.hasOption("antiAliasing"));
			builder.enablePngCrush(commandLine.hasOption("enablePngCrush"));
			builder.postConvertWebp(commandLine.hasOption("postWebpConvert"));

			return builder.build();
		} catch (Exception e) {
			System.err.println("Could not parse args: " + e.getMessage());
		}
		return null;
	}

	private static void printHelp(Options options) {
		HelpFormatter help = new HelpFormatter();
		help.setWidth(110);
		help.setLeftPadding(4);
		help.printHelp("converter", "version: " + CLInterpreter.class.getPackage().getImplementationVersion(), options, "", true);
	}

	private static Options setupOptions() {
		Options options = new Options();

		Option srcOpt = Option.builder(SOURCE_ARG).required().argName("path to file or folder").hasArg(true).desc("The source. Can be an image file or a folder containing image files to be converted. This argument is mandatory.").build();
		Option srcScaleOpt = Option.builder(SCALE_ARG).argName("float").hasArg(true).desc("The source scrScale factor (1,1.5,2,3,4,etc.), ie. the base scrScale used to calculate if images need to be up- or downscaled. Ie. if you have the src file in density xxxhdpi you pass '4'. This argument is mandatory.").build();
		Option dstOpt = Option.builder(DST_ARG).hasArg(true).argName("path").desc("The directory in which the converted files will be written. Will use the source folder if this argument is omitted.").build();

		Option platform = Option.builder(PLATFORM_ARG).hasArg(true).argName("all|android|ios").desc("Can be 'all', 'android' or 'ios'. Sets what formats the converted images will be generated for. E.g. set 'android' if you only want to convert to android format. Default is " + Arguments.DEFAULT_PLATFORM).build();
		Option threadCount = Option.builder(THREADS_ARG).argName("1-8").hasArg(true).desc("Sets the count of max parallel threads (more is faster but uses more memory). Possible values are 1-8. Default is " + Arguments.DEFAULT_THREAD_COUNT).build();
		Option roundingHandler = Option.builder(ROUNDING_MODE_ARG).argName("round|ceil|floor").hasArg(true).desc("Defines the rounding mode when scaling the dimensions. Possible options are 'round' (rounds up of >= 0.5), 'floor' (rounds down) and 'ceil' (rounds up). Default is " + Arguments.DEFAULT_ROUNDING_STRATEGY).build();
		Option compression = Option.builder(OUT_COMPRESSION_ARG).hasArg(true).argName("png|jpg").desc("Sets the compression of the converted images. Can be 'png', 'jpg', 'gif' or 'png+jpg'. By default the src compression type will be used (e.g. png will be re-compressed to png after scaling).").build();
		Option compressionQuality = Option.builder(COMPRESSION_QUALITY_ARG).hasArg(true).argName("0.0-1.0").desc("Only used with compression 'jpg' sets the quality [0-1.0] where 1.0 is the highest quality. Default is " + Arguments.DEFAULT_COMPRESSION_QUALITY).build();

		Option skipExistingFiles = Option.builder(SKIP_EXISTING_ARG).desc("If set will not overwrite a already existing file").build();
		Option includeObsoleteFormats = Option.builder("androidIncludeLdpiTvdpi").desc("Android only: If set will include additional densities (ldpi and tvdpi).").build();
		Option mipmapInsteadOfDrawable = Option.builder("androidMipmapInsteadOfDrawable").desc("Android only: creates mipmap sub-folders instead of drawable.").build();
		Option skipUpscaling = Option.builder("skipUpscaling").desc("If set will only scale down, but not up to prevent image quality loss").build();
		Option verboseLog = Option.builder(VERBOSE_ARG).desc("If set will log to console more verbose").build();
		Option haltOnError = Option.builder("haltOnError").desc("If set will stop the process if an error occurred during conversion").build();
		Option antiAliasing = Option.builder("antiAliasing").desc("Anti-aliases images creating a little more blurred result; useful for very small images").build();
		Option enablePngCrush = Option.builder("enablePngCrush").desc("Will post-process all pngs with pngcrush. The executable must be set in the system path as 'pngcrush' i.e executable from every path. Pngcrush is a tool to compress pngs. Requires v1.7.22+").build();
		Option postWebpConvert = Option.builder("postWebpConvert").desc("Will additionally convert all png/gif to lossless wepb and all jpg to lossy webp with cwebp. Does not delete source files. The executable must be set in the system path as 'cwebp' i.e executable from every path. cwebp is the official converter from Google.").build();

		Option help = Option.builder("h").longOpt("help").desc("This help page").build();
		Option version = Option.builder("v").longOpt("version").desc("Gets current version").build();
		Option gui = Option.builder("gui").desc("Starts graphical user interface").build();

		OptionGroup mainArgs = new OptionGroup();
		mainArgs.addOption(srcOpt).addOption(help).addOption(version).addOption(gui);
		mainArgs.setRequired(true);

		options.addOption(srcScaleOpt).addOption(dstOpt);
		options.addOption(platform).addOption(compression).addOption(compressionQuality).addOption(threadCount).addOption(roundingHandler);
		options.addOption(skipExistingFiles).addOption(skipUpscaling).addOption(includeObsoleteFormats).addOption(verboseLog).addOption(antiAliasing)
				.addOption(haltOnError).addOption(mipmapInsteadOfDrawable).addOption(enablePngCrush).addOption(postWebpConvert);

		options.addOptionGroup(mainArgs);

		return options;
	}
}
