package at.favre.tools.converter;

import at.favre.tools.converter.arg.Arguments;
import at.favre.tools.converter.arg.EPlatform;
import at.favre.tools.converter.platforms.AndroidConverter;
import at.favre.tools.converter.platforms.ConverterCallback;
import at.favre.tools.converter.platforms.IOSConverter;
import at.favre.tools.converter.platforms.IPlatformConverter;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Main Converter class
 */
public class ConverterHandler {
	private CountDownLatch latch;
	private LocalCallback converterCallback;
	private HandlerCallback handlerCallback;
	private long beginMs;

	public void execute(Arguments args, HandlerCallback callback) {
		beginMs = System.currentTimeMillis();
		handlerCallback = callback;

		System.out.println("\nbegin execution using " + args.threadCount + " theads\n");

		List<IPlatformConverter> converters = new ArrayList<>();

		ExecutorService threadPool = new ThreadPoolExecutor(args.threadCount, args.threadCount, 5, TimeUnit.SECONDS, new ArrayBlockingQueue<>(256));

		if (args.platform == EPlatform.ANROID || args.platform == EPlatform.ALL) {
			converters.add(new AndroidConverter());
		}
		if (args.platform == EPlatform.IOS || args.platform == EPlatform.ALL) {
			converters.add(new IOSConverter());
		}

		int jobs = args.filesToProcess.size() * converters.size();
		boolean halted = false;
		latch = new CountDownLatch(jobs);
		converterCallback = new LocalCallback(args, latch, jobs, callback);

		for (File srcFile : args.filesToProcess) {
			if (!srcFile.exists() || !srcFile.isFile()) {
				throw new IllegalStateException("srcFile " + srcFile + " does not exist");
			}

			BufferedImage srcImage = null;
			try {
				srcImage = ConverterUtil.loadImage(srcFile.getAbsolutePath());

				for (IPlatformConverter converter : converters) {
					threadPool.execute(new ConverterWorker(converter, srcFile, srcImage, args, converterCallback));
				}
			} catch (Exception e) {
				System.err.println("Could not load or convert " + ConverterUtil.getWithoutExtension(srcFile) + ": " + e.getMessage());

				if (args.haltOnError) {
					halted = true;
					while (latch.getCount() > 0) {
						latch.countDown();
					}
					break;
				}
			}
		}

		threadPool.shutdown();

		try {
			latch.await(10, TimeUnit.MINUTES);
			informFinished(halted);
		} catch (InterruptedException e) {
			System.err.println("Timeout will waiting for execution to finish: " + e.getMessage());
		}
	}

	private void informFinished(boolean halted) {
		if (handlerCallback != null) {
			handlerCallback.onFinished(converterCallback.getFinished(), converterCallback.getExceptions(), (System.currentTimeMillis() - beginMs), halted);
		}
	}

	private static class LocalCallback implements ConverterCallback {
		private Arguments arguments;
		private CountDownLatch latch;
		private final int jobCount;
		private int finished = 0;
		private HandlerCallback callback;
		private List<Exception> exceptions;

		public LocalCallback(Arguments arguments, CountDownLatch latch, int jobCount, HandlerCallback callback) {
			this.arguments = arguments;
			this.latch = latch;
			this.jobCount = jobCount;
			this.callback = callback;
			this.exceptions = new ArrayList<>();
		}

		@Override
		public void success(String log) {
			if (arguments.verboseLog) {
				System.out.printf(log);
			}
			jobFinished();
		}

		@Override
		public void failure(Exception e) {
			System.err.println("Error in convert worker: " + e.getMessage());
			e.printStackTrace();
			exceptions.add(e);
			jobFinished();
		}

		private void jobFinished() {
			latch.countDown();
			finished++;
			if (callback != null) {
				callback.onProgress((float) finished / (float) jobCount);
			}
		}

		public List<Exception> getExceptions() {
			return exceptions;
		}

		public int getFinished() {
			return finished;
		}
	}

	private static class ConverterWorker implements Runnable {
		private final IPlatformConverter converter;
		private final File srcFile;
		private final BufferedImage srcRawImage;
		private final Arguments arguments;
		private final ConverterCallback callback;

		public ConverterWorker(IPlatformConverter converter, File srcFile, BufferedImage srcRawImage, Arguments arguments, ConverterCallback callback) {
			this.converter = converter;
			this.srcFile = srcFile;
			this.srcRawImage = srcRawImage;
			this.arguments = arguments;
			this.callback = callback;
		}

		@Override
		public void run() {
			converter.convert(arguments.dst, srcRawImage, ConverterUtil.getWithoutExtension(srcFile), Arguments.getSrcCompressionType(srcFile), arguments, callback);
		}
	}

	public interface HandlerCallback {
		void onProgress(float progress);

		void onFinished(int finsihedJobs, List<Exception> exceptions, long time, boolean haltedDuringProcess);
	}
}
