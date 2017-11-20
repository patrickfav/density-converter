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

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Misc util methods
 */
public final class MiscUtil {
    private MiscUtil() {
    }

    public static String getStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        return sw.toString();
    }

    public static String duration(long ms) {
        if (ms >= 1000) {
            return String.format(Locale.US, "%.2f sec", (double) ms / 1000);
        }
        return ms + " ms";
    }

    public static <T> T[] concat(T[] first, T[] second) {
        T[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    public static File createAndCheckFolder(String path, boolean dryRun) {
        File f = new File(path);

        if (dryRun) {
            return f;
        }

        if (!f.exists()) {
            f.mkdirs();
        }

        if (!f.exists() || !f.isDirectory()) {
            throw new IllegalStateException("could not create folder: " + path);
        }
        return f;
    }

    public static String getFileExtensionLowerCase(File file) {
        return getFileExtension(file).toLowerCase();
    }

    public static String getFileExtension(File file) {
        if (file == null) {
            return "";
        }
        return file.getName().substring(file.getName().lastIndexOf(".") + 1);
    }

    public static String getFileNameWithoutExtension(File file) {
        String fileName = file.getName();
        int pos = fileName.lastIndexOf(".");
        if (pos > 0) {
            fileName = fileName.substring(0, pos);
        }
        return fileName;
    }

    public static String getCmdProgressBar(float progress) {
        int loadingBarCount = 40;
        int bars = Math.round((float) loadingBarCount * progress);
        StringBuilder sb = new StringBuilder("\r[");

        for (int i = 0; i < loadingBarCount; i++) {
            if (i < bars) {
                sb.append("-");
            } else {
                sb.append(" ");
            }
        }
        sb.append("] ");

        if (progress < 1f) {
            sb.append(String.format("%6s", String.format(Locale.US, "%.2f", progress * 100f))).append("%");
        } else {
            sb.append("100.00%\n");
        }

        return sb.toString();
    }

    public static <T> Set<T> toSet(T elem) {
        Set<T> set = new HashSet<>(1);
        set.add(elem);
        return set;
    }

    public static void deleteFolder(File folder) {
        if (folder != null && folder.exists()) {
            File[] files = folder.listFiles();
            if (files != null) { //some JVMs return null for empty dirs
                for (File f : files) {
                    if (f.isDirectory()) {
                        deleteFolder(f);
                    } else {
                        f.delete();
                    }
                }
            }
            folder.delete();
        }
    }
}
