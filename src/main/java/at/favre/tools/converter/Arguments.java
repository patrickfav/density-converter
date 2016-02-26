package at.favre.tools.converter;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Handles all the arguments that can be set in the converter
 */
public class Arguments {
	public static final float DEFAULT_COMPRESSION_QUALITY = 0.9f;
	public static final int DEFAULT_THREAD_COUNT = 2;
	public static final RoundingHandler.Strategy DEFAULT_ROUNDING_STRATEGY = RoundingHandler.Strategy.ROUND_HALF_UP;
	public static final Platform DEFAULT_PLATFORM = Platform.ALL;

	public final static String[] VALID_FILE_EXTENSIONS = new String[]{Compression.GIF.name().toLowerCase(), Compression.JPG.name().toLowerCase(), Compression.PNG.name().toLowerCase(), "jpeg"};

	public final static Arguments START_GUI = new Arguments();

	public final File src;
	public final File dst;
	public final float scrScale;
	public final Platform platform;
	public final OutputCompressionMode outputCompressionMode;
	public final float compressionQuality;
	public final int threadCount;
	public final boolean skipExistingFiles;
	public final boolean skipUpscaling;
	public final boolean verboseLog;
	public final boolean includeObsoleteFormats;
	public final boolean haltOnError;
	public final RoundingHandler roundingHandler;
	public final List<File> filesToProcess;

	public Arguments(File src, File dst, float scrScale, Platform platform, OutputCompressionMode outputCompressionMode,
	                 float compressionQuality, int threadCount, boolean skipExistingFiles, boolean skipUpscaling,
	                 boolean verboseLog, boolean includeObsoleteFormats, boolean haltOnError, RoundingHandler roundingHandler) {
		this.dst = dst;
		this.src = src;
		this.scrScale = scrScale;
		this.platform = platform;
		this.outputCompressionMode = outputCompressionMode;
		this.compressionQuality = compressionQuality;
		this.threadCount = threadCount;
		this.skipExistingFiles = skipExistingFiles;
		this.skipUpscaling = skipUpscaling;
		this.verboseLog = verboseLog;
		this.includeObsoleteFormats = includeObsoleteFormats;
		this.haltOnError = haltOnError;
		this.roundingHandler = roundingHandler;

		this.filesToProcess = new ArrayList<>();

		if (src != null && src.isDirectory()) {
			for (File file : src.listFiles()) {
				String extension = ConverterUtil.getFileExtension(file);
				if (Arrays.asList(VALID_FILE_EXTENSIONS).contains(extension)) {
					filesToProcess.add(file);
					if (verboseLog) {
						System.out.println("add " + file + " to processing queue");
					}
				}
			}
		} else {
			filesToProcess.add(src);
		}
	}

	private Arguments() {
		this(null, null, 0f, null, null, 0f, 0, false, false, false, false, false, null);
	}

	@Override
	public String toString() {
		return "Arguments{" +
				"src=" + src +
				", dst=" + dst +
				", scrScale=" + scrScale +
				", platform=" + platform +
				", outputCompressionType=" + outputCompressionMode +
				", compressionQuality=" + compressionQuality +
				", threadCount=" + threadCount +
				", skipExistingFiles=" + skipExistingFiles +
				", skipUpscaling=" + skipUpscaling +
				", verboseLog=" + verboseLog +
				", includeObsoleteFormats=" + includeObsoleteFormats +
				", haltOnError=" + haltOnError +
				", roundingHandler=" + roundingHandler +
				'}';
	}

	public enum Platform {
		ALL, ANROID, IOS
	}

	public enum OutputCompressionMode {
		SAME_AS_INPUT, JPG, PNG, GIF, JPG_AND_PNG
	}

	public enum Compression {
		JPG, PNG, GIF
	}

	public static class Builder {
		private File dst;
		private float srcScale;
		private File src = null;
		private Platform platform = DEFAULT_PLATFORM;
		private OutputCompressionMode outputCompressionMode = OutputCompressionMode.SAME_AS_INPUT;
		private float compressionQuality = DEFAULT_COMPRESSION_QUALITY;
		private int threadCount = DEFAULT_THREAD_COUNT;
		private RoundingHandler.Strategy roundingStrategy = DEFAULT_ROUNDING_STRATEGY;
		private boolean skipExistingFiles = false;
		private boolean skipUpscaling = false;
		private boolean verboseLog = false;
		private boolean includeObsoleteFormats = false;
		private boolean haltOnError = false;

		public Builder(File src, float srcScale) {
			this.src = src;
			this.srcScale = srcScale;
		}

		public Builder dstFolder(File dst) {
			this.dst = dst;
			return this;
		}

		public Builder platform(Platform platform) {
			this.platform = platform;
			return this;
		}

		public Builder compression(OutputCompressionMode outputCompressionMode) {
			this.outputCompressionMode = outputCompressionMode;
			return this;
		}

		public Builder compression(OutputCompressionMode outputCompressionMode, float compressionQuality) {
			this.outputCompressionMode = outputCompressionMode;
			this.compressionQuality = compressionQuality;
			return this;
		}

		public Builder threadCount(int threadCount) {
			this.threadCount = threadCount;
			return this;
		}

		public Builder skipExistingFiles() {
			this.skipExistingFiles = true;
			return this;
		}

		public Builder skipUpscaling() {
			this.skipUpscaling = true;
			return this;
		}

		public Builder verboseLog() {
			this.verboseLog = true;
			return this;
		}

		public Builder includeObsoleteFormats() {
			this.includeObsoleteFormats = true;
			return this;
		}

		public Builder haltOnError() {
			this.haltOnError = true;
			return this;
		}

		public Builder scaleRoundingStragy(RoundingHandler.Strategy strategy) {
			this.roundingStrategy = strategy;
			return this;
		}

		public Arguments build() throws InvalidArgumentException {
			if (src == null || !src.exists()) {
				throw new InvalidArgumentException("src file/directory must be passed and should exist: " + src);
			}

			if (dst == null) {
				if (src.isDirectory()) {
					dst = src;
				} else {
					dst = src.getParentFile();
				}
			}

			if (compressionQuality < 0 || compressionQuality > 1.0) {
				throw new InvalidArgumentException("invalid compression quality argument '" + compressionQuality + "' - must be between (including) 0 and 1.0");
			}

			if (threadCount < 1 || threadCount > 8) {
				throw new InvalidArgumentException("invalid thread count given '" + threadCount + "' - must be between (including) 1 and 8");
			}

			if (srcScale < 0 || srcScale > 100) {
				throw new InvalidArgumentException("invalid src scale given '" + srcScale + "' - must be between (excluding) 0 and 100");
			}

			return new Arguments(src, dst, srcScale, platform, outputCompressionMode, compressionQuality, threadCount, skipExistingFiles, skipUpscaling,
					verboseLog, includeObsoleteFormats, haltOnError, new RoundingHandler(roundingStrategy));
		}
	}

	public static Compression getSrcCompressionType(File srcFile) {
		String extension = ConverterUtil.getFileExtension(srcFile);
		switch (extension) {
			case "jpg":
			case "jpeg":
				return Compression.JPG;
			case "png":
				return Compression.PNG;
			case "gif":
				return Compression.GIF;
			default:
				throw new IllegalArgumentException("unknown file extension " + extension + " in srcFile " + srcFile);
		}
	}

	public static List<Compression> getCompressionForType(OutputCompressionMode type, Compression srcCompression) {
		List<Compression> list = new ArrayList<>(2);
		switch (type) {
			case GIF:
				list.add(Compression.GIF);
				break;
			case PNG:
				list.add(Compression.PNG);
				break;
			case JPG:
				list.add(Compression.JPG);
				break;
			case JPG_AND_PNG:
				list.add(Compression.JPG);
				list.add(Compression.PNG);
				break;
			default:
			case SAME_AS_INPUT:
				list.add(srcCompression);
				break;
		}
		return list;
	}
}
