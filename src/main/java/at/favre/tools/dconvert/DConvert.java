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

package at.favre.tools.dconvert;

import at.favre.tools.dconvert.arg.Arguments;
import at.favre.tools.dconvert.arg.EPlatform;
import at.favre.tools.dconvert.converters.IPlatformConverter;
import at.favre.tools.dconvert.converters.postprocessing.IPostProcessor;
import at.favre.tools.dconvert.converters.postprocessing.MozJpegProcessor;
import at.favre.tools.dconvert.converters.postprocessing.PngCrushProcessor;
import at.favre.tools.dconvert.converters.postprocessing.WebpProcessor;
import at.favre.tools.dconvert.converters.scaling.ImageHandler;
import at.favre.tools.dconvert.converters.scaling.ScaleAlgorithm;
import at.favre.tools.dconvert.util.MiscUtil;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import java.io.File;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * This is the main class handling all of the converters and post processors.
 * This handles the threading and orchestration of the threads.
 * <p>
 * All user interfaces will call this class to execute.
 */
public class DConvert {
    private CountDownLatch mainLatch;

    private HandlerCallback handlerCallback;
    private long beginMs;
    private StringBuilder logStringBuilder = new StringBuilder();

    /**
     * Starts the execution of the dconvert
     *
     * @param args                  from user interface
     * @param blockingWaitForFinish if true will block the thread until all threads are finished
     * @param callback              main callback
     */
    public void execute(Arguments args, boolean blockingWaitForFinish, HandlerCallback callback) {
        beginMs = System.currentTimeMillis();
        handlerCallback = callback;

        logStringBuilder.append("registered image readers:\n").append(getRegisteredImageReadersAndWriters()).append("\n");
        logStringBuilder.append("begin execution using ").append(args.threadCount).append(" theads\n");
        logStringBuilder.append("args: ").append(args).append("\n");

        if (!args.filesToProcess.isEmpty()) {
            List<IPlatformConverter> converters = new ArrayList<>();
            List<IPostProcessor> postProcessors = new ArrayList<>();

            for (EPlatform ePlatform : args.platform) {
                logStringBuilder.append("add ").append(ePlatform.getConverter().getClass().getSimpleName()).append("\n");
                converters.add(ePlatform.getConverter());
            }

            if (args.clearDirBeforeConvert) {
                logStringBuilder.append("clear out dirs before convert\n");
                for (IPlatformConverter converter : converters) {
                    converter.clean(args);
                }
            }

            if (args.enablePngCrush) {
                IPostProcessor postProcessor = new PngCrushProcessor();
                if (postProcessor.isSupported()) {
                    logStringBuilder.append("add pngcrush postprocessor\n");
                    postProcessors.add(postProcessor);
                } else {
                    logStringBuilder.append("WARNING: Tool 'pngcrush' cannot be accessed. Is it set in PATH?\n");
                }
            }
            if (args.postConvertWebp) {
                IPostProcessor postProcessor = new WebpProcessor();
                if (postProcessor.isSupported()) {
                    logStringBuilder.append("add cwebp postprocessor\n");
                    postProcessors.add(postProcessor);
                } else {
                    logStringBuilder.append("WARNING: Tool 'cwebp' cannot be accessed. Is it set in PATH?\n");
                }
            }
            if (args.enableMozJpeg) {
                IPostProcessor postProcessor = new MozJpegProcessor();
                if (postProcessor.isSupported()) {
                    logStringBuilder.append("add mozJpeg postprocessor\n");
                    postProcessors.add(postProcessor);
                } else {
                    logStringBuilder.append("WARNING: Tool 'jpegtran' cannot be accessed. Is it set in PATH?\n");
                }
            }

            int convertJobs = args.filesToProcess.size() * converters.size();
            int postProcessorJobs = convertJobs * postProcessors.size();

            float convertPercentage = (float) convertJobs / (float) (convertJobs + postProcessorJobs);
            float postProcessPercentage = (float) postProcessorJobs / (float) (convertJobs + postProcessorJobs);

            mainLatch = new CountDownLatch(1);

            for (File srcFile : args.filesToProcess) {
                logStringBuilder.append("add ").append(srcFile).append(" to processing queue\n");

                if (!srcFile.exists() || !srcFile.isFile()) {
                    throw new IllegalStateException("srcFile " + srcFile + " does not exist");
                }
            }

            new WorkerHandler<>(converters, args, new WorkerHandler.Callback() {
                @Override
                public void onProgress(float percent) {
                    handlerCallback.onProgress(convertPercentage * percent);
                }

                @Override
                public void onFinished(final int finishedJobsConverters, List<File> outFiles, final StringBuilder logConverters, final List<Exception> exceptionsConverters, final boolean haltedDuringProcessConverters) {
                    logStringBuilder.append(logConverters);
                    if (haltedDuringProcessConverters) {
                        informFinished(finishedJobsConverters, exceptionsConverters, true);
                    } else {
                        new WorkerHandler<>(postProcessors, args, new WorkerHandler.Callback() {
                            @Override
                            public void onProgress(float percent) {
                                handlerCallback.onProgress(convertPercentage + (postProcessPercentage * percent));
                            }

                            @Override
                            public void onFinished(int finishedJobsPostProcessors, List<File> outFiles, StringBuilder log, List<Exception> exceptions, boolean haltedDuringProcess) {
                                exceptionsConverters.addAll(exceptions);
                                logStringBuilder.append(log);
                                informFinished(finishedJobsPostProcessors + finishedJobsConverters, exceptionsConverters, haltedDuringProcess);
                            }
                        }).start(outFiles);
                    }
                }
            }).start(args.filesToProcess);

            if (blockingWaitForFinish) {
                try {
                    mainLatch.await(60, TimeUnit.MINUTES);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } else {
            logStringBuilder.append("no files to convert\n");
            informFinished(0, Collections.emptyList(), false);
        }
    }

    private void informFinished(int finishedJobs, List<Exception> exceptions, boolean haltedDuringProcess) {
        System.gc();
        printTrace();
        if (handlerCallback != null) {
            if (mainLatch != null) {
                mainLatch.countDown();
            }
            for (Exception exception : exceptions) {
                logStringBuilder.append(MiscUtil.getStackTrace(exception)).append("\n");
            }
            handlerCallback.onFinished(finishedJobs, exceptions, (System.currentTimeMillis() - beginMs), haltedDuringProcess, logStringBuilder.toString().trim());
        }
    }

    public interface HandlerCallback {
        void onProgress(float progress);

        void onFinished(int finishedJobs, List<Exception> exceptions, long time, boolean haltedDuringProcess, String log);
    }

    private String getRegisteredImageReadersAndWriters() {
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

    private void printTrace() {
        if (ImageHandler.TEST_MODE) {
            for (Map.Entry<ScaleAlgorithm, Long> entry : ImageHandler.traceMap.entrySet()) {
                System.out.println(entry.getKey() + ": " + String.format(Locale.US, "%.2f", (double) entry.getValue() / 1000000.0));
            }
        }
    }
}
