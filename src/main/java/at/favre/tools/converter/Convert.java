package at.favre.tools.converter;

import at.favre.tools.converter.graphics.CompressionType;
import at.favre.tools.converter.platforms.AndroidConverter;
import at.favre.tools.converter.platforms.IOSConverter;
import at.favre.tools.converter.platforms.IPlatformConverter;
import org.apache.commons.cli.*;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Main Converter class
 */
public class Convert {
	public final static String[] VALID_FILE_EXTENSIONS = new String[]{CompressionType.GIF.formatName, CompressionType.JPEG.formatName, CompressionType.PNG.formatName, "jpeg"};

	public static void main(String[] rawArgs) {
		Options options = setupOptions();

		Arguments args = parse(options, rawArgs);

//		File srcFile = new File("C:\\Users\\PatrickF\\Desktop\\");

		if (args.verboseLog) {
			System.out.println("Arguments: " + args + "\n");
		}

		List<File> filesToProcess = new ArrayList<>();

		if (args.src.isDirectory()) {
			for (File file : args.src.listFiles()) {
				String extension = ConverterUtil.getFileExtension(file);
				if (Arrays.asList(VALID_FILE_EXTENSIONS).contains(extension)) {
					filesToProcess.add(file);
					System.out.println("add " + file + " to processing queue");
				}
			}
		} else {
			filesToProcess.add(args.src);
		}

		execute(args, filesToProcess);
	}

	private static Arguments parse(Options options, String[] args) {
		Arguments.Builder builder;
		CommandLineParser parser = new DefaultParser();
		try {
			CommandLine commandLine = parser.parse(options, args);

			builder = new Arguments.Builder(new File(commandLine.getOptionValue("src")), Float.valueOf(commandLine.getOptionValue("scrScale")));
			builder.dstFolder(new File(commandLine.getOptionValue("dst")));

			float compressionQuality = Arguments.DEFAULT_COMPRESSION_QUALITY;
			if (commandLine.hasOption("compressionQuality")) {
				compressionQuality = Float.valueOf(commandLine.getOptionValue("compressionQuality"));
			}

			if (commandLine.hasOption("compression")) {
				switch (commandLine.getOptionValue("compression")) {
					case "png":
						builder.compression(Arguments.OutputCompressionMode.PNG);
						break;
					case "jpg":
						builder.compression(Arguments.OutputCompressionMode.JPG, compressionQuality);
						break;
					case "gif":
						builder.compression(Arguments.OutputCompressionMode.GIF);
						break;
					case "jpg+png":
						builder.compression(Arguments.OutputCompressionMode.JPG_AND_PNG, compressionQuality);
						break;
					default:
						System.err.println("unknown compression type: " + commandLine.getOptionValue("compression"));
				}
			}

			if (commandLine.hasOption("mode")) {
				switch (commandLine.getOptionValue("mode")) {
					case "all":
						builder.platform(Arguments.Platform.ALL);
						break;
					case "android":
						builder.platform(Arguments.Platform.ANROID);
						break;
					case "ios":
						builder.platform(Arguments.Platform.IOS);
						break;
					default:
						System.err.println("unknown mode: " + commandLine.getOptionValue("mode"));
				}
			}

			return builder.build();
		} catch (Exception e) {
			System.err.println("Could not parse args: " + e.getMessage());
		}
		return null;
	}

	private static Options setupOptions() {
		Options options = new Options();

		Option srcOpt = Option.builder("src").required().hasArg(true).desc("The source. Can be an image file or a folder containing image files to be converted").build();
		Option srcScaleOpt = Option.builder("scrScale").required().hasArg(true).desc("The source scrScale factor (1,1.5,2,3,4,etc.), ie. the base scrScale used to calculate if images need to be up- or downscaled. Ie. if you have the src file in density xxxhdpi you pass '4'").build();
		Option dstOpt = Option.builder("dst").required().hasArg(true).desc("The directory in which the converted files will be written").build();
		Option mode = Option.builder("mode").hasArg(true).desc("Can be 'all', 'android' or 'ios'. Decide what formats the converted images will be generated for.").build();
		Option compression = Option.builder("compression").hasArg(true).desc("Sets the compression of the converted images. Can be 'png', 'jpg' or 'gif'. By default the src compression type will be used.").build();
		Option compressionQuality = Option.builder("compressionQuality").hasArg(true).desc("Only used with compression 'jpg' sets the quality [0-1.0] where 1.0 is the highest quality").build();

		options.addOption(srcOpt);
		options.addOption(srcScaleOpt);
		options.addOption(dstOpt);
		options.addOption(mode);
		options.addOption(compression);
		options.addOption(compressionQuality);

		return options;
	}

	private static void execute(Arguments args, List<File> srcFiles) {
		final long begin = System.currentTimeMillis();
		System.out.println("\nbegin execution\n");

		List<IPlatformConverter> converters = new ArrayList<>();

		ExecutorService threadPool = new ThreadPoolExecutor(args.threadCount, args.threadCount, 5, TimeUnit.SECONDS, new ArrayBlockingQueue<>(256));

		if (args.platform == Arguments.Platform.ANROID || args.platform == Arguments.Platform.ALL) {
			converters.add(new AndroidConverter());
		}
		if (args.platform == Arguments.Platform.IOS || args.platform == Arguments.Platform.ALL) {
			converters.add(new IOSConverter());
		}

		for (File srcFile : srcFiles) {
			if (!srcFile.exists() || !srcFile.isFile()) {
				throw new IllegalStateException("srcFile " + srcFile + " does not exist");
			}

			for (IPlatformConverter converter : converters) {
				threadPool.execute(new ConverterWorker(converter, args));
			}
		}

		threadPool.shutdown();
		try {
			threadPool.awaitTermination(30, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("execution finished (" + (System.currentTimeMillis() - begin) + "ms)");
	}



	private static class ConverterWorker implements Runnable {
		private IPlatformConverter converter;
		private Arguments arguments;

		public ConverterWorker(IPlatformConverter converter, Arguments arguments) {
			this.converter = converter;
			this.arguments = arguments;
		}

		@Override
		public void run() {
			try {
				BufferedImage srcImage = ConverterUtil.loadImage(arguments.src.getAbsolutePath());
				converter.convert(arguments.dst, srcImage, ConverterUtil.getWithoutExtension(arguments.src), Arguments.getSrcCompressionType(arguments.src), arguments, null);
			} catch (Exception e) {
				System.out.println("Could not load or convert " + ConverterUtil.getWithoutExtension(arguments.src) + ": " + e.getMessage());
			}
		}
	}
}
