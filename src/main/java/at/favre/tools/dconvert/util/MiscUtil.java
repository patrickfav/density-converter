package at.favre.tools.dconvert.util;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Locale;

/**
 * Misc util methods
 */
public class MiscUtil {
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

	public static File createAndCheckFolder(String path) {
		File f = new File(path);

		if (!f.exists()) {
			f.mkdir();
		}

		if (!f.exists() || !f.isDirectory()) {
			throw new IllegalStateException("could not create folder: " + path);
		}
		return f;
	}

	public static String getFileExtensionLowerCase(File file) {
		if (file == null) {
			return "";
		}
		return file.getName().substring(file.getName().lastIndexOf(".") + 1).toLowerCase();
	}

	public static String getFileNameWithoutExtension(File file) {
		String fileName = file.getName();
		int pos = fileName.lastIndexOf(".");
		if (pos > 0) {
			fileName = fileName.substring(0, pos);
		}
		return fileName;
	}
}
