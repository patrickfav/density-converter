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

package at.favre.tools.dconvert.ui;

import at.favre.tools.dconvert.arg.*;
import org.apache.commons.cli.*;

import java.io.File;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * Handles parsing of command line arguments
 */
public final class CLIInterpreter {
    public static final String COMPRESSION_QUALITY_ARG = "compressionQuality";
    public static final String THREADS_ARG = "threads";
    public static final String SOURCE_ARG = "src";
    public static final String SCALE_ARG = "scale";
    public static final String PLATFORM_ARG = "platform";
    public static final String UPSCALING_ALGO_ARG = "algorithmUpscaling";
    public static final String DOWNSCALING_ALGO_ARG = "algorithmDownscaling";
    public static final String OUT_COMPRESSION_ARG = "outCompression";
    public static final String ROUNDING_MODE_ARG = "roundingMode";
    public static final String DST_ARG = "dst";
    public static final String VERBOSE_ARG = "verbose";
    public static final String SKIP_EXISTING_ARG = "skipExisting";
    public static final String SCALE_IS_HEIGHT_DP_ARG = "scaleIsHeightDp";

    private CLIInterpreter() {
    }

    public static Arguments parse(String[] args) {
        ResourceBundle strings = ResourceBundle.getBundle("bundles.strings", Locale.getDefault());
        Options options = setupOptions(strings);
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
                System.out.println("Version: " + CLIInterpreter.class.getPackage().getImplementationVersion());
                return null;
            }

            String scaleRawParam = commandLine.getOptionValue(SCALE_ARG).toLowerCase();

            boolean dp = false;

            if (scaleRawParam.contains("dp")) {
                dp = true;
                scaleRawParam = scaleRawParam.replace("dp", "").trim();
            }

            builder = new Arguments.Builder(new File(commandLine.getOptionValue(SOURCE_ARG)), Float.parseFloat(scaleRawParam));

            if (dp && commandLine.hasOption(SCALE_IS_HEIGHT_DP_ARG)) {
                builder.scaleMode(EScaleMode.DP_HEIGHT);
            } else if (dp && !commandLine.hasOption(SCALE_IS_HEIGHT_DP_ARG)) {
                builder.scaleMode(EScaleMode.DP_WIDTH);
            } else {
                builder.scaleMode(EScaleMode.FACTOR);
            }

            if (commandLine.hasOption(DST_ARG)) {
                builder.dstFolder(new File(commandLine.getOptionValue(DST_ARG)));
            }

            float compressionQuality = Arguments.DEFAULT_COMPRESSION_QUALITY;
            if (commandLine.hasOption(COMPRESSION_QUALITY_ARG)) {
                compressionQuality = Float.valueOf(commandLine.getOptionValue(COMPRESSION_QUALITY_ARG));
            }

            if (commandLine.hasOption(OUT_COMPRESSION_ARG)) {
                switch (commandLine.getOptionValue(OUT_COMPRESSION_ARG)) {
                    case "strict":
                        builder.compression(EOutputCompressionMode.SAME_AS_INPUT_STRICT);
                        break;
                    case "png":
                        builder.compression(EOutputCompressionMode.AS_PNG);
                        break;
                    case "jpg":
                        builder.compression(EOutputCompressionMode.AS_JPG, compressionQuality);
                        break;
                    case "gif":
                        builder.compression(EOutputCompressionMode.AS_GIF);
                        break;
                    case "bmp":
                        builder.compression(EOutputCompressionMode.AS_BMP);
                        break;
                    case "png+jpg":
                        builder.compression(EOutputCompressionMode.AS_JPG_AND_PNG, compressionQuality);
                        break;
                    default:
                        System.err.println("unknown compression type: " + commandLine.getOptionValue(OUT_COMPRESSION_ARG));
                }
            }

            Set<EPlatform> platformSet = new HashSet<>(EPlatform.values().length);
            if (commandLine.hasOption(PLATFORM_ARG)) {
                switch (commandLine.getOptionValue(PLATFORM_ARG)) {
                    case "all":
                        platformSet = EPlatform.getAll();
                        break;
                    case "android":
                        platformSet.add(EPlatform.ANDROID);
                        break;
                    case "ios":
                        platformSet.add(EPlatform.IOS);
                        break;
                    case "win":
                        platformSet.add(EPlatform.WINDOWS);
                        break;
                    case "web":
                        platformSet.add(EPlatform.WEB);
                        break;
                    default:
                        System.err.println("unknown mode: " + commandLine.getOptionValue(PLATFORM_ARG));
                }
                builder.platform(platformSet);
            }

            if (commandLine.hasOption(UPSCALING_ALGO_ARG)) {
                builder.upScaleAlgorithm(EScalingAlgorithm.getByName(commandLine.getOptionValue(UPSCALING_ALGO_ARG)));
            }

            if (commandLine.hasOption(DOWNSCALING_ALGO_ARG)) {
                builder.downScaleAlgorithm(EScalingAlgorithm.getByName(commandLine.getOptionValue(DOWNSCALING_ALGO_ARG)));
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
            builder.enablePngCrush(commandLine.hasOption("postProcessorPngCrush"));
            builder.postConvertWebp(commandLine.hasOption("postProcessorWebp"));
            builder.dryRun(commandLine.hasOption("dryRun"));
            builder.enableMozJpeg(commandLine.hasOption("postProcessorMozJpeg"));
            builder.keepUnoptimizedFilesPostProcessor(commandLine.hasOption("keepOriginalPostProcessedFiles"));
            builder.iosCreateImagesetFolders(commandLine.hasOption("iosCreateImagesetFolders"));
            builder.clearDirBeforeConvert(commandLine.hasOption("clean"));

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
        help.printHelp("converter", "version: " + CLIInterpreter.class.getPackage().getImplementationVersion(), options, "", true);
    }

    private static Options setupOptions(ResourceBundle bundle) {
        Options options = new Options();

        Option srcOpt = Option.builder(SOURCE_ARG).required().argName("path to file or folder").hasArg(true).desc(bundle.getString("arg.descr.cmd.src")).build();
        Option srcScaleOpt = Option.builder(SCALE_ARG).argName("[float]|[int]dp").hasArg(true).desc(bundle.getString("arg.descr.cmd.scale")).build();
        Option dstOpt = Option.builder(DST_ARG).hasArg(true).argName("path").desc(bundle.getString("arg.descr.cmd.dst")).build();

        Option platform = Option.builder(PLATFORM_ARG).hasArg(true).argName("all|android|ios|win|web").desc(MessageFormat.format(bundle.getString("arg.descr.cmd.platform"), Arguments.DEFAULT_PLATFORM)).build();
        Option threadCount = Option.builder(THREADS_ARG).argName("1-8").hasArg(true).desc(MessageFormat.format(bundle.getString("arg.descr.cmd.threads"), String.valueOf(Arguments.DEFAULT_THREAD_COUNT))).build();
        Option roundingHandler = Option.builder(ROUNDING_MODE_ARG).argName("round|ceil|floor").hasArg(true).desc(MessageFormat.format(bundle.getString("arg.descr.cmd.rounding"), Arguments.DEFAULT_ROUNDING_STRATEGY)).build();
        Option compression = Option.builder(OUT_COMPRESSION_ARG).hasArg(true).argName("png|jpg|gif|bmp").desc(bundle.getString("arg.descr.cmd.outcompression")).build();
        Option compressionQuality = Option.builder(COMPRESSION_QUALITY_ARG).hasArg(true).argName("0.0-1.0").desc(MessageFormat.format(bundle.getString("arg.descr.cmd.compression"), String.valueOf(Arguments.DEFAULT_COMPRESSION_QUALITY))).build();
        Option upScalingAlgo = Option.builder(UPSCALING_ALGO_ARG).hasArg(true).argName(EScalingAlgorithm.getCliArgString(EScalingAlgorithm.Type.UPSCALING)).desc(MessageFormat.format(bundle.getString("arg.descr.scalingalgo"), Arguments.DEFAULT_PLATFORM)).build();
        Option downScalingAlgo = Option.builder(DOWNSCALING_ALGO_ARG).hasArg(true).argName(EScalingAlgorithm.getCliArgString(EScalingAlgorithm.Type.DOWNSCALING)).desc(MessageFormat.format(bundle.getString("arg.descr.scalingalgo"), Arguments.DEFAULT_PLATFORM)).build();

        Option skipExistingFiles = Option.builder(SKIP_EXISTING_ARG).desc(bundle.getString("arg.descr.skipexisting")).build();
        Option androidIncludeLdpiTvdpi = Option.builder("androidIncludeLdpiTvdpi").desc(bundle.getString("arg.descr.androidmipmap")).build();
        Option mipmapInsteadOfDrawable = Option.builder("androidMipmapInsteadOfDrawable").desc(bundle.getString("arg.descr.androidldpi")).build();
        Option iosCreateImagesetFolders = Option.builder("iosCreateImagesetFolders").desc(bundle.getString("arg.descr.iosimageset")).build();
        Option skipUpscaling = Option.builder("skipUpscaling").desc(bundle.getString("arg.descr.skipupscaling")).build();
        Option verboseLog = Option.builder(VERBOSE_ARG).desc(bundle.getString("arg.descr.cmd.verbose")).build();
        Option haltOnError = Option.builder("haltOnError").desc(bundle.getString("arg.descr.halterror")).build();
        Option antiAliasing = Option.builder("antiAliasing").desc(bundle.getString("arg.descr.antialiasing")).build();
        Option enablePngCrush = Option.builder("postProcessorPngCrush").desc(bundle.getString("arg.descr.pngcrush")).build();
        Option postWebpConvert = Option.builder("postProcessorWebp").desc(bundle.getString("arg.descr.webp")).build();
        Option keepUnPostProcessed = Option.builder("keepOriginalPostProcessedFiles").desc(bundle.getString("arg.descr.keeporiginal")).build();
        Option dpScaleIsHeight = Option.builder(SCALE_IS_HEIGHT_DP_ARG).desc(bundle.getString("arg.descr.cmd.dpIsHeight")).build();
        Option dryRun = Option.builder("dryRun").desc(bundle.getString("arg.descr.dryrun")).build();
        Option enableMozJpeg = Option.builder("postProcessorMozJpeg").desc(bundle.getString("arg.descr.mozjpeg")).build();
        Option cleanBeforeConvert = Option.builder("clean").desc(bundle.getString("arg.descr.clean")).build();

        Option help = Option.builder("h").longOpt("help").desc(bundle.getString("arg.descr.cmd.help")).build();
        Option version = Option.builder("v").longOpt("version").desc(bundle.getString("arg.descr.cmd.version")).build();
        Option gui = Option.builder("gui").desc(bundle.getString("arg.descr.cmd.gui")).build();

        OptionGroup mainArgs = new OptionGroup();
        mainArgs.addOption(srcOpt).addOption(help).addOption(version).addOption(gui);
        mainArgs.setRequired(true);

        options.addOption(srcScaleOpt).addOption(dstOpt);
        options.addOption(platform).addOption(compression).addOption(compressionQuality).addOption(threadCount).addOption(roundingHandler)
                .addOption(upScalingAlgo).addOption(downScalingAlgo);
        options.addOption(skipExistingFiles).addOption(skipUpscaling).addOption(androidIncludeLdpiTvdpi).addOption(verboseLog)
                .addOption(antiAliasing).addOption(dryRun).addOption(haltOnError).addOption(mipmapInsteadOfDrawable)
                .addOption(enablePngCrush).addOption(postWebpConvert).addOption(dpScaleIsHeight).addOption(enableMozJpeg)
                .addOption(keepUnPostProcessed).addOption(iosCreateImagesetFolders).addOption(cleanBeforeConvert);

        options.addOptionGroup(mainArgs);

        return options;
    }
}
