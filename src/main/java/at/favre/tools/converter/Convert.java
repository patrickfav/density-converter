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

	enum Mode {
		ALL, ANDROID, IOS
	}

	private static ExecutorService THREAD_POOL = new ThreadPoolExecutor(2, 8, 2, TimeUnit.SECONDS, new ArrayBlockingQueue<>(256));

	private static class Args {
		public File dst;
		public File src;
		public float scale;
		public Mode mode = Mode.ALL;
		public CompressionType compressionType = null;
		public float compressionQuality = 0.9f;

		@Override
		public String toString() {
			return "Args{" +
					"dst=" + dst +
					", src=" + src +
					", scale=" + scale +
					", mode=" + mode +
					", compressionType=" + compressionType +
					", compressionQuality=" + compressionQuality +
					'}';
		}
	}

	public static void main(String[] args) {
		Options options = setupOptions();

		Args argsObj = parse(options, args);

//		File srcFile = new File("C:\\Users\\PatrickF\\Desktop\\");

		System.out.println("Arguments: " + argsObj + "\n");

		List<File> filesToProcess = new ArrayList<>();

		if (argsObj.src.isDirectory()) {
			for (File file : argsObj.src.listFiles()) {
				String extension = ConverterUtil.getFileExtension(file);
				if (Arrays.asList(VALID_FILE_EXTENSIONS).contains(extension)) {
					filesToProcess.add(file);
					System.out.println("add " + file + " to processing queue");
				}
			}
		} else {
			filesToProcess.add(argsObj.src);
		}

		execute(argsObj.dst, argsObj.mode, filesToProcess, argsObj.scale, argsObj.compressionType == null,
				new RoundingHandler(RoundingHandler.Strategy.ROUND), argsObj.compressionType, argsObj.compressionQuality);
	}

	private static Args parse(Options options, String[] args) {
		Args argsObject = new Args();
		CommandLineParser parser = new DefaultParser();
		try {
			CommandLine commandLine = parser.parse(options, args);

			argsObject.src = new File(commandLine.getOptionValue("src"));
			argsObject.dst = new File(commandLine.getOptionValue("dst"));
			argsObject.scale = Float.valueOf(commandLine.getOptionValue("scale"));

			if (commandLine.hasOption("compression")) {
				switch (commandLine.getOptionValue("compression")) {
					case "png":
						argsObject.compressionType = CompressionType.PNG;
						break;
					case "jpg":
						argsObject.compressionType = CompressionType.JPEG;
						break;
					case "gif":
						argsObject.compressionType = CompressionType.GIF;
						break;
					default:
						System.err.println("unknown compression type: " + commandLine.getOptionValue("compression"));
				}
			}

			if (commandLine.hasOption("compressionQuality")) {
				argsObject.compressionQuality = Float.valueOf(commandLine.getOptionValue("compressionQuality"));
			}

			if (commandLine.hasOption("mode")) {
				switch (commandLine.getOptionValue("mode")) {
					case "all":
						argsObject.mode = Mode.ALL;
						break;
					case "android":
						argsObject.mode = Mode.ANDROID;
						break;
					case "ios":
						argsObject.mode = Mode.IOS;
						break;
					default:
						System.err.println("unknown mode: " + commandLine.getOptionValue("mode"));
				}
			}

		} catch (ParseException e) {
			System.err.println("Could not parse args: " + e.getMessage());
		}

		return argsObject;
	}

	private static Options setupOptions() {
		Options options = new Options();

		Option srcOpt = Option.builder("src").required().hasArg(true).desc("The source. Can be an image file or a folder containing image files to be converted").build();
		Option srcScaleOpt = Option.builder("scale").required().hasArg(true).desc("The source scale factor (1,1.5,2,3,4,etc.), ie. the base scale used to calculate if images need to be up- or downscaled. Ie. if you have the src file in density xxxhdpi you pass '4'").build();
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

	private static void execute(File root, Mode mode, List<File> srcFiles, double baseScale, boolean useSameCompressionAsSrc, RoundingHandler roundingHandler, CompressionType compressionType, float compressionQuality) {
		final long begin = System.currentTimeMillis();
		System.out.println("\nbegin execution\n");

		AndroidConverter androidConverter = new AndroidConverter();
		androidConverter.setup(roundingHandler);

		IOSConverter iosConverter = new IOSConverter();
		iosConverter.setup(roundingHandler);


		for (File srcFile : srcFiles) {
			if (!srcFile.exists() || !srcFile.isFile()) {
				throw new IllegalStateException("srcFile " + srcFile + " does not exist");
			}

			CompressionType srcCompressionType = getSrcCompressionType(srcFile);

			if (mode == Mode.ALL || mode == Mode.ANDROID) {
				THREAD_POOL.execute(new ConverterWorker(androidConverter, root, baseScale, srcFile, ConverterUtil.getWithoutExtension(srcFile), useSameCompressionAsSrc ? srcCompressionType : compressionType, compressionQuality));
			}
			if (mode == Mode.ALL || mode == Mode.IOS) {
				THREAD_POOL.execute(new ConverterWorker(iosConverter, root, baseScale, srcFile, ConverterUtil.getWithoutExtension(srcFile), useSameCompressionAsSrc ? srcCompressionType : compressionType, compressionQuality));
			}
		}

		THREAD_POOL.shutdown();
		try {
			THREAD_POOL.awaitTermination(30, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("execution finished (" + (System.currentTimeMillis() - begin) + "ms)");
	}

	private static CompressionType getSrcCompressionType(File srcFile) {
		String extension = ConverterUtil.getFileExtension(srcFile);
		switch (extension) {
			case "jpg":
			case "jpeg":
				return CompressionType.JPEG;
			case "png":
				return CompressionType.PNG;
			case "gif":
				return CompressionType.GIF;
			default:
				throw new IllegalArgumentException("unknown file extension " + extension + " in srcFile " + srcFile);
		}
	}

	private static class ConverterWorker implements Runnable {
		private IPlatformConverter converter;
		private File dstFolder;
		private double baseScale;
		private File srcFile;
		private String targetFileName;
		private CompressionType compressionType;
		private float compressionQuality;

		public ConverterWorker(IPlatformConverter converter, File dstFolder, double baseScale, File srcFile, String targetFileName, CompressionType compressionType, float compressionQuality) {
			this.converter = converter;
			this.dstFolder = dstFolder;
			this.baseScale = baseScale;
			this.srcFile = srcFile;
			this.targetFileName = targetFileName;
			this.compressionType = compressionType;
			this.compressionQuality = compressionQuality;
		}

		@Override
		public void run() {
			try {
				BufferedImage srcImage = ConverterUtil.loadImage(srcFile.getAbsolutePath());
				converter.convert(dstFolder, srcImage, baseScale, targetFileName, compressionType, compressionQuality);
			} catch (Exception e) {
				System.out.println("Could not load or convert " + targetFileName + ": " + e.getMessage());
			}
		}
	}
}
