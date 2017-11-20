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

package at.favre.tools.dconvert.util;

import at.favre.tools.dconvert.arg.Arguments;
import at.favre.tools.dconvert.arg.ImageType;
import at.favre.tools.dconvert.converters.Result;
import at.favre.tools.dconvert.converters.postprocessing.IPostProcessor;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Util for post processors
 */
public final class PostProcessorUtil {
    private static ReentrantLock lock = new ReentrantLock();

    private PostProcessorUtil() {
    }

    public static Result runImageOptimizer(File rawFile, ImageType processedType, String[] args, boolean keepOriginal) throws IOException {
        return runImageOptimizer(rawFile, processedType, args, keepOriginal, MiscUtil.getFileExtension(rawFile));
    }

    public static Result runImageOptimizer(File rawFile, ImageType processedType, String[] args, boolean keepOriginal, String outExtension) throws IOException {
        if (Arguments.getImageType(rawFile) == processedType && rawFile.exists() && rawFile.isFile()) {
            String id = UUID.randomUUID().toString().substring(0, 8);

            File outFile = getFileWithPostFix(rawFile, "_optimized_" + id, outExtension);
            File copy = getFileWithPostFix(rawFile, "_copy_" + id, outExtension);

            Files.copy(rawFile.toPath(), copy.toPath(), StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);

            for (int i = 0; i < args.length; i++) {
                if (args[i].equals("%%outFilePath%%")) {
                    args[i] = "\"" + outFile.getAbsolutePath() + "\"";
                }

                if (args[i].equals("%%sourceFilePath%%")) {
                    args[i] = "\"" + copy.getAbsolutePath() + "\"";
                }
            }

            Result result = runCmd(args);

            copy.delete();

            boolean r1 = true, r2 = true, r3 = true;
            if (outFile.exists() && outFile.isFile()) {
                if (keepOriginal) {
                    File origFile = getFileWithPostFix(rawFile, IPostProcessor.ORIG_POSTFIX, MiscUtil.getFileExtension(rawFile));

                    if (origFile.exists()) {
                        origFile.delete();
                    }

                    r1 = rawFile.renameTo(origFile);

                    File outFileNew = getFileWithPostFix(rawFile, "", outExtension);

                    if (outFileNew.exists()) {
                        outFileNew.delete();
                    }

                    r2 = outFile.renameTo(outFileNew);
                } else {
                    if (rawFile.delete()) {
                        File outFileNew = getFileWithPostFix(rawFile, "", outExtension);

                        if (outFileNew.exists()) {
                            outFileNew.delete();
                        }

                        r3 = outFile.renameTo(outFileNew);
                    }
                }
            }
            String log = result.log;
            if (!r1 || !r2 || !r3) {
                log += "Could not rename all files correctly\n";
            }

            return new Result(log, result.exception, Collections.singletonList(rawFile));
        }
        return null;
    }

    private static File getFileWithPostFix(File src, String postfix, String extension) {
        return new File(src.getParentFile(), MiscUtil.getFileNameWithoutExtension(src) + postfix + "." + extension);
    }

    private static Result runCmd(String[] cmdArray) {
        StringBuilder logStringBuilder = new StringBuilder();
        Exception exception = null;
        try {
            logStringBuilder.append("execute: ").append(Arrays.toString(cmdArray)).append("\n");
            ProcessBuilder pb = new ProcessBuilder(cmdArray);
            pb.redirectErrorStream(true);
            Process process = pb.start();
            try (BufferedReader inStreamReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String s;
                while ((s = inStreamReader.readLine()) != null) {
                    if (!s.isEmpty()) logStringBuilder.append("\t").append(s).append("\n");
                }
            }
            process.waitFor();
        } catch (Exception e) {
            exception = e;
            logStringBuilder.append("error: could not run command - ").append(Arrays.toString(cmdArray)).append(" - ").append(e.getMessage()).append(" - is it set in PATH?\n");
        }
        return new Result(logStringBuilder.toString(), exception, Collections.emptyList());
    }

    public static boolean canRunCmd(String[] cmdArray) {
        try {
            ProcessBuilder pb = new ProcessBuilder(cmdArray);
            pb.redirectErrorStream(true);
            Process process = pb.start();
            try (BufferedReader inStreamReader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                while ((inStreamReader.readLine()) != null) {
                }
            }
            process.waitFor();
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}
