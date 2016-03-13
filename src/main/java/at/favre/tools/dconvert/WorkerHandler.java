package at.favre.tools.dconvert;

import at.favre.tools.dconvert.arg.Arguments;
import at.favre.tools.dconvert.converters.IPlatformConverter;
import at.favre.tools.dconvert.converters.Result;
import at.favre.tools.dconvert.converters.postprocessing.IPostProcessor;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Handles post processing tasks
 */
public class WorkerHandler<T> {

	private final List<T> processors;
	private final ExecutorService threadPool;
	private final Arguments arguments;
	private final int jobCount;
	private final Callback callback;

	public WorkerHandler(List<T> processors, List<File> allFiles, Callback callback, Arguments arguments) {
		this.processors = processors;
		this.threadPool = new ThreadPoolExecutor(arguments.threadCount, arguments.threadCount, 5, TimeUnit.SECONDS, new ArrayBlockingQueue<>(1024 * 10));
		this.jobCount = allFiles.size() * processors.size();
		this.callback = callback;
		this.arguments = arguments;
		start(allFiles);
	}

	private void start(List<File> allFiles) {
		InternalCallback internalCallback = new InternalCallback(callback);

		for (T processor : processors) {
			for (File fileToProcess : allFiles) {
				threadPool.execute(new Worker(fileToProcess, processor, arguments, internalCallback));
			}
		}

		threadPool.shutdown();

		if (jobCount == 0) {
			callback.onFinished(0, Collections.emptyList(), new StringBuilder(), Collections.emptyList(), false);
		}
	}

	private class Worker implements Runnable {
		private File unprocessedFile;
		private T processor;
		private InternalCallback callback;
		private final Arguments arguments;

		public Worker(File unprocessedFile, T processors, Arguments arguments, InternalCallback callback) {
			this.unprocessedFile = unprocessedFile;
			this.arguments = arguments;
			this.processor = processors;
			this.callback = callback;
		}

		@Override
		public void run() {
			Result result = null;
			if (IPostProcessor.class.isInstance(processor)) {
				result = ((IPostProcessor) processor).process(unprocessedFile, arguments.keepUnoptimizedFilesPostProcessor);
			} else if (IPlatformConverter.class.isInstance(processor)) {
				result = ((IPlatformConverter) processor).convert(unprocessedFile, arguments);
			}
			callback.onJobFinished(result);
		}
	}

	private class InternalCallback {
		private int currentJobCount = 0;
		private List<Exception> exceptionList = new ArrayList<>();
		private Callback callback;
		private StringBuilder logBuilder = new StringBuilder();
		private boolean canceled = false;
		private List<File> files = new ArrayList<>();

		public InternalCallback(Callback callback) {
			this.callback = callback;
		}

		synchronized void onJobFinished(Result result) {
			if (!canceled) {
				currentJobCount++;

				if (result != null) {
					if (result.log != null && result.log.length() > 0) {
						logBuilder.append(result.log).append("\n");
					}
					if (result.processedFiles != null) {
						files.addAll(result.processedFiles);
					}
					if (result.exception != null) {
						exceptionList.add(result.exception);

						if (arguments.haltOnError) {
							canceled = true;
							threadPool.shutdownNow();
							callback.onFinished(currentJobCount, files, logBuilder, exceptionList, true);
						}
					}
				}

				if (!canceled) {
					if (currentJobCount == jobCount) {
						callback.onFinished(currentJobCount, files, logBuilder, exceptionList, false);
					} else {
						callback.onProgress((float) currentJobCount / (float) jobCount);
					}
				}
			}
		}
	}

	public interface Callback {
		void onProgress(float percent);

		void onFinished(int finishedJobs, List<File> outFiles, StringBuilder log, List<Exception> exceptions, boolean haltedDuringProcess);
	}
}
