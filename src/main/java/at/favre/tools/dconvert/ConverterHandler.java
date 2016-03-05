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

package at.favre.tools.dconvert;

import at.favre.tools.dconvert.arg.Arguments;
import at.favre.tools.dconvert.arg.EPlatform;
import at.favre.tools.dconvert.converters.AndroidConverter;
import at.favre.tools.dconvert.converters.ConverterCallback;
import at.favre.tools.dconvert.converters.IOSConverter;
import at.favre.tools.dconvert.converters.IPlatformConverter;
import at.favre.tools.dconvert.converters.postprocessing.PngCrushProcessor;
import at.favre.tools.dconvert.converters.postprocessing.PostProcessor;
import at.favre.tools.dconvert.converters.postprocessing.WebpProcessor;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import java.io.File;
import java.util.*;
import java.util.concurrent.*;

/**
 * This is the main class handling all of the converters and post processors.
 * This handles the threading and orchestration of the threads.
 * <p>
 * All user interfaces will call this class to execute.
 */
public class ConverterHandler {
	private CountDownLatch mainLatch;

	private LocalCallback converterCallback;
	private HandlerCallback handlerCallback;
	private Arguments arguments;
	private long beginMs;
	private StringBuilder logStringBuilder = new StringBuilder();
	private List<PostProcessor> postProcessors = new ArrayList<>();

	/**
	 * Starts the execution of the dconvert
	 *
	 * @param args                  from user interface
	 * @param callback              main callback
	 * @param blockingWaitForFinish if true will block the thread until all threads are finished
	 */
	public void execute(Arguments args, HandlerCallback callback, boolean blockingWaitForFinish) {
		arguments = args;
		beginMs = System.currentTimeMillis();
		handlerCallback = callback;

		logStringBuilder.append("registered image readers:\n").append(getRegisteredImageReadersAndWriters()).append("\n");
		logStringBuilder.append("begin execution using ").append(args.threadCount).append(" theads\n");
		logStringBuilder.append("args: ").append(args).append("\n");

		if (!args.filesToProcess.isEmpty()) {
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
		} else {
			logStringBuilder.append("no files to convert\n");
			informFinished(logStringBuilder.toString(), false);
		}
	}

	private void informFinished(String log, boolean halted) {
		System.gc();
		if (handlerCallback != null) {
			if (converterCallback != null) {
				handlerCallback.onFinished(converterCallback.getFinished(), converterCallback.getExceptions(), (System.currentTimeMillis() - beginMs), halted, log);
			} else {
				handlerCallback.onFinished(0, Collections.emptyList(), (System.currentTimeMillis() - beginMs), halted, log);
			}
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
			logSB.append("error: ").append(e.getClass().getSimpleName()).append(" / ").append(e.getMessage()).append("\n");
			e.printStackTrace();
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
		if (!postProcessors.isEmpty()) {
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
		} else {
			informFinished(log.toString(), false);
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
				converter.convert(srcFile, arguments, callback);
			} catch (Exception e) {
				callback.failure(e);
			}
		}
	}

	public interface HandlerCallback {
		void onProgress(float progress, String log);

		void onFinished(int finsihedJobs, List<Exception> exceptions, long time, boolean haltedDuringProcess, String log);
	}

	public String getRegisteredImageReadersAndWriters() {
		String[] formats = new String[]{"JPEG", "PNG", "TIFF", "PSD", "SVG", "BMP"};

		StringBuilder sb = new StringBuilder();
		for (String format : Arrays.asList(formats)) {
			Iterator<ImageReader> reader = ImageIO.getImageReadersByFormatName(format);
			while (reader.hasNext()) {
				ImageReader next = reader.next();
				sb.append("reader: ").append(next).append("\n");
			}
			Iterator<ImageWriter> writer = ImageIO.getImageWritersByFormatName(format);
			while (writer.hasNext()) {
				ImageWriter next = writer.next();
				sb.append("writer: ").append(next).append("\n");
			}
		}
		return sb.toString();
	}
}
