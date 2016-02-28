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
	private StringBuilder logStringBuilder = new StringBuilder();

	public void execute(Arguments args, HandlerCallback callback) {
		arguments = args;
		beginMs = System.currentTimeMillis();
		handlerCallback = callback;

		logStringBuilder.append("begin execution using ").append(args.threadCount).append(" theads\n");
		logStringBuilder.append("args: ").append(args).append("\n");

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
		converterCallback = new LocalCallback(jobs, callback, threadPool, logStringBuilder);

		for (File srcFile : args.filesToProcess) {
			logStringBuilder.append("add ").append(srcFile).append(" to processing queue\n");

			if (!srcFile.exists() || !srcFile.isFile()) {
				throw new IllegalStateException("srcFile " + srcFile + " does not exist");
			}

			for (IPlatformConverter converter : converters) {
				threadPool.execute(new ConverterWorker(converter, srcFile, args, converterCallback));
			}
		}
		threadPool.shutdown();
	}

	private void informFinished(String log, boolean halted) {
		if (handlerCallback != null) {
			handlerCallback.onFinished(converterCallback.getFinished(), converterCallback.getExceptions(), (System.currentTimeMillis() - beginMs), halted, log);
		}
	}

	private class LocalCallback implements ConverterCallback {
		private final int jobCount;
		private int finished = 0;
		private HandlerCallback callback;
		private List<Exception> exceptions;
		private ExecutorService threadPool;
		private boolean done = false;
		private final StringBuilder logSB;

		public LocalCallback(int jobCount, HandlerCallback callback, ExecutorService threadPool, StringBuilder logStringBuilder) {
			this.jobCount = jobCount;
			this.callback = callback;
			this.threadPool = threadPool;
			this.exceptions = new ArrayList<>();
			this.logSB = logStringBuilder;
		}

		@Override
		public void success(String log) {
			logSB.append(log).append("\n");
			jobFinished(log);
		}

		@Override
		public void failure(Exception e) {
			exceptions.add(e);
			jobFinished(null);
			logSB.append("error: ").append(e.getMessage()).append("\n");
			if (arguments.haltOnError) {
				done = true;
				threadPool.shutdownNow();
				informFinished(logSB.toString(), true);
			}
		}

		private void jobFinished(String log) {
			if (!done) {
				latch.countDown();
				finished++;
				if (callback != null) {
					callback.onProgress((float) finished / (float) jobCount, log);
				}
				if (latch.getCount() == 0) {
					done = true;
					informFinished(logSB.toString(), false);
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
		void onProgress(float progress, String log);

		void onFinished(int finsihedJobs, List<Exception> exceptions, long time, boolean haltedDuringProcess, String log);
	}
}
