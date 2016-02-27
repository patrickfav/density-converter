package at.favre.tools.converter;

import at.favre.tools.converter.arg.Arguments;
import at.favre.tools.converter.arg.EPlatform;
import at.favre.tools.converter.platforms.AndroidConverter;
import at.favre.tools.converter.platforms.ConverterCallback;
import at.favre.tools.converter.platforms.IOSConverter;
import at.favre.tools.converter.platforms.IPlatformConverter;

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
	private Arguments arguments;
	private long beginMs;

	public void execute(Arguments args, HandlerCallback callback) {
		arguments = args;
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
		latch = new CountDownLatch(jobs);
		converterCallback = new LocalCallback(jobs, callback, threadPool);

		for (File srcFile : args.filesToProcess) {
			if (!srcFile.exists() || !srcFile.isFile()) {
				throw new IllegalStateException("srcFile " + srcFile + " does not exist");
			}

			for (IPlatformConverter converter : converters) {
				threadPool.execute(new ConverterWorker(converter, srcFile, args, converterCallback));
			}
		}
		threadPool.shutdown();
	}

	private void informFinished(boolean halted) {
		if (handlerCallback != null) {
			handlerCallback.onFinished(converterCallback.getFinished(), converterCallback.getExceptions(), (System.currentTimeMillis() - beginMs), halted);
		}
	}

	private class LocalCallback implements ConverterCallback {
		private final int jobCount;
		private int finished = 0;
		private HandlerCallback callback;
		private List<Exception> exceptions;
		private ExecutorService threadPool;
		private boolean done = false;

		public LocalCallback(int jobCount, HandlerCallback callback, ExecutorService threadPool) {
			this.jobCount = jobCount;
			this.callback = callback;
			this.threadPool = threadPool;
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

			if (arguments.verboseLog) {
				e.printStackTrace();
			}

			exceptions.add(e);
			jobFinished();

			if (arguments.haltOnError) {
				done = true;
				threadPool.shutdownNow();
				informFinished(true);
			}
		}

		private void jobFinished() {
			if (!done) {
				latch.countDown();
				finished++;
				if (callback != null) {
					callback.onProgress((float) finished / (float) jobCount);
				}
				if (latch.getCount() == 0) {
					done = true;
					informFinished(false);
				}
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
		private final Arguments arguments;
		private final ConverterCallback callback;

		public ConverterWorker(IPlatformConverter converter, File srcFile, Arguments arguments, ConverterCallback callback) {
			this.converter = converter;
			this.srcFile = srcFile;
			this.arguments = arguments;
			this.callback = callback;
		}

		@Override
		public void run() {
			try {
				converter.convert(arguments.dst, ConverterUtil.loadImage(srcFile.getAbsolutePath()), ConverterUtil.getWithoutExtension(srcFile), Arguments.getSrcCompressionType(srcFile), arguments, callback);
			} catch (Exception e) {
				callback.failure(e);
			}
		}
	}

	public interface HandlerCallback {
		void onProgress(float progress);

		void onFinished(int finsihedJobs, List<Exception> exceptions, long time, boolean haltedDuringProcess);
	}
}
