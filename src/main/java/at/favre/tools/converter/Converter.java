package at.favre.tools.converter;

import at.favre.tools.converter.platforms.AndroidConverter;
import at.favre.tools.converter.platforms.IOSConverter;
import at.favre.tools.converter.platforms.IPlatformConverter;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Main Converter class
 */
public class Converter {

	public static void main(String[] rawArgs) {
		Arguments args = CLInterpreter.parse(rawArgs);

		if (args == null) {
			return;
		} else if (args == Arguments.START_GUI) {
			System.out.println("start gui");
			//TODO start ui
			return;
		}

		if (args.verboseLog) {
			System.out.println("\nArguments: " + args + "\n");
		}

		execute(args);
	}


	private static void execute(Arguments args) {
		final long begin = System.currentTimeMillis();
		System.out.println("\nbegin execution using " + args.threadCount + " theads\n");

		List<IPlatformConverter> converters = new ArrayList<>();

		ExecutorService threadPool = new ThreadPoolExecutor(args.threadCount, args.threadCount, 5, TimeUnit.SECONDS, new ArrayBlockingQueue<>(256));

		if (args.platform == Arguments.Platform.ANROID || args.platform == Arguments.Platform.ALL) {
			converters.add(new AndroidConverter());
		}
		if (args.platform == Arguments.Platform.IOS || args.platform == Arguments.Platform.ALL) {
			converters.add(new IOSConverter());
		}

		for (File srcFile : args.filesToProcess) {
			if (!srcFile.exists() || !srcFile.isFile()) {
				throw new IllegalStateException("srcFile " + srcFile + " does not exist");
			}

			BufferedImage srcImage = null;
			try {
				srcImage = ConverterUtil.loadImage(srcFile.getAbsolutePath());

				for (IPlatformConverter converter : converters) {
					threadPool.execute(new ConverterWorker(converter, srcFile, srcImage, args));
				}
			} catch (Exception e) {
				System.err.println("Could not load or convert " + ConverterUtil.getWithoutExtension(srcFile) + ": " + e.getMessage());

				if (args.haltOnError) {
					System.err.println("stop execution");
					break;
				}
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
		private File srcFile;
		private BufferedImage srcRawImage;
		private Arguments arguments;

		public ConverterWorker(IPlatformConverter converter, File srcFile, BufferedImage srcRawImage, Arguments arguments) {
			this.converter = converter;
			this.srcFile = srcFile;
			this.srcRawImage = srcRawImage;
			this.arguments = arguments;
		}

		@Override
		public void run() {
			converter.convert(arguments.dst, srcRawImage, ConverterUtil.getWithoutExtension(srcFile), Arguments.getSrcCompressionType(srcFile), arguments, null);
		}
	}
}
