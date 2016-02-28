package at.favre.tools.converter;

import at.favre.tools.converter.arg.Arguments;
import at.favre.tools.converter.arg.EPlatform;
import at.favre.tools.converter.converters.AndroidConverter;
import at.favre.tools.converter.converters.ConverterCallback;
import at.favre.tools.converter.converters.IOSConverter;
import at.favre.tools.converter.converters.IPlatformConverter;
import at.favre.tools.converter.converters.postprocessing.PngCrushProcessor;
import at.favre.tools.converter.converters.postprocessing.PostProcessor;
import at.favre.tools.converter.converters.postprocessing.WebpProcessor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Main Converter class
 */
public class ConverterHandler {
	private CountDownLatch mainLatch;

	private LocalCallback converterCallback;
	private HandlerCallback handlerCallback;
	private Arguments arguments;
	private long beginMs;
	private StringBuilder logStringBuilder = new StringBuilder();
	private List<PostProcessor> postProcessors = new ArrayList<>();

	public void execute(Arguments args, HandlerCallback callback, boolean blockingWaitForFinish) {
		arguments = args;
		beginMs = System.currentTimeMillis();
		handlerCallback = callback;

		logStringBuilder.append("begin execution using ").append(args.threadCount).append(" theads\n");
		logStringBuilder.append("args: ").append(args).append("\n");

		List<IPlatformConverter> converters = new ArrayList<>();

		ExecutorService threadPool = new ThreadPoolExecutor(args.threadCount, args.threadCount, 5, TimeUnit.SECONDS, new ArrayBlockingQueue<>(256));

		if (args.platform == EPlatform.ANROID || args.platform == EPlatform.ALL) {
			logStringBuilder.append("add android converter\n");
			converters.add(new AndroidConverter());
		}
		if (args.platform == EPlatform.IOS || args.platform == EPlatform.ALL) {
			logStringBuilder.append("add ios converter\n");
			converters.add(new IOSConverter());
		}

		if (args.enablePngCrush) {
			logStringBuilder.append("add pngcrush postprocessor\n");
			postProcessors.add(new PngCrushProcessor());
		}
		if (args.postConvertWebp) {
			logStringBuilder.append("add webp postprocessor\n");
			postProcessors.add(new WebpProcessor());
		}

		int convertJobs = args.filesToProcess.size() * converters.size();
		int allJobs = convertJobs + (convertJobs * postProcessors.size());

		mainLatch = new CountDownLatch(allJobs);
		converterCallback = new LocalCallback(allJobs, new CountDownLatch(convertJobs), threadPool, logStringBuilder);

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

		if (blockingWaitForFinish) {
			try {
				mainLatch.await(30, TimeUnit.MINUTES);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void informFinished(String log, boolean halted) {
		System.gc();
		if (handlerCallback != null) {
			handlerCallback.onFinished(converterCallback.getFinished(), converterCallback.getExceptions(), (System.currentTimeMillis() - beginMs), halted, log);
		}
	}

	private class LocalCallback implements ConverterCallback {
		private final int jobCount;
		private int finished = 0;
		private List<Exception> exceptions;
		private ExecutorService threadPool;
		private boolean done = false;
		private final StringBuilder logSB;
		private List<List<File>> resultingFiles = new ArrayList<>();
		private CountDownLatch latch;

		public LocalCallback(int jobCount, CountDownLatch latch, ExecutorService threadPool, StringBuilder logStringBuilder) {
			this.jobCount = jobCount;
			this.threadPool = threadPool;
			this.exceptions = new ArrayList<>();
			this.logSB = logStringBuilder;
			this.latch = latch;
		}

		@Override
		public void success(String log, List<File> compressedImages) {
			logSB.append(log).append("\n");
			resultingFiles.add(compressedImages);
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
				mainLatch.countDown();
				finished++;
				if (handlerCallback != null) {
					handlerCallback.onProgress((float) finished / (float) jobCount, log);
				}
				if (latch.getCount() == 0) {
					done = true;
					startPostProcessing(resultingFiles, logSB, finished, jobCount);
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

	private void startPostProcessing(List<List<File>> allFiles, StringBuilder log, final int finishedJobs, final int jobCount) {
		log.append("\nstart post processing\n");

		new Thread(() -> {
			int finished = finishedJobs;
			for (List<File> filesPerConvert : allFiles) {
				for (File file : filesPerConvert) {
					for (PostProcessor postProcessor : postProcessors) {
						String currentLog = postProcessor.process(file);
						log.append(currentLog).append("\n");
					}
				}
				mainLatch.countDown();
				finished++;
				handlerCallback.onProgress((float) finished / (float) jobCount, "");
			}
			informFinished(log.toString(), false);
		}).start();
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
				converter.convert(arguments.dst, ConverterUtil.loadImage(srcFile.getAbsolutePath()), ConverterUtil.getFileNameWithoutExtension(srcFile), Arguments.getCompressionType(srcFile), arguments, callback);
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
