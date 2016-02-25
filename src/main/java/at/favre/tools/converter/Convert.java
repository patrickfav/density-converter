package at.favre.tools.converter;

import at.favre.tools.converter.graphics.CompressionType;
import at.favre.tools.converter.platforms.AndroidConverter;
import at.favre.tools.converter.platforms.IOSConverter;
import at.favre.tools.converter.platforms.IPlatformConverter;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by PatrickF on 25.02.2016.
 */
public class Convert {
	public final static String[] VALID_FILE_EXTENSIONS = new String[] {CompressionType.GIF.formatName,CompressionType.JPEG.formatName,CompressionType.PNG.formatName,"jpeg"};

	enum Mode {
		ALL, ANDROID, IOS
	}


	private static ExecutorService THREAD_POOL = new ThreadPoolExecutor(2, 8, 2, TimeUnit.SECONDS, new ArrayBlockingQueue<>(256));

	public static void main(String[] args) {

		File root = ConverterUtil.createAndCheckFolder("C:\\test");
		File srcFile = new File("C:\\Users\\PatrickF\\Desktop\\");

		List<File> filesToProcess = new ArrayList<>();

		if(srcFile.isDirectory()) {
			for (File file : srcFile.listFiles()) {
				String extension = ConverterUtil.getFileExtension(file);
				if(Arrays.asList(VALID_FILE_EXTENSIONS).contains(extension)) {
					filesToProcess.add(file);
					System.out.println("add "+file+" to processing queue");
				}
			}
		} else {
			filesToProcess.add(srcFile);
		}

		execute(root, Collections.singleton(Mode.ALL),filesToProcess,2, true,
				new RoundingHandler(RoundingHandler.Strategy.ROUND), CompressionType.PNG,0.9f);
	}

	private static void execute(File root, Set<Mode> modes, List<File> srcFiles, double baseScale, boolean useSameCompressionAsSrc, RoundingHandler roundingHandler, CompressionType compressionType, float compressionQuality) {
		final long begin = System.currentTimeMillis();
		System.out.println("begin execution");

		AndroidConverter androidConverter = new AndroidConverter();
		androidConverter.setup(roundingHandler);

		IOSConverter iosConverter = new IOSConverter();
		iosConverter.setup(roundingHandler);


		for (File srcFile : srcFiles) {
			if (!srcFile.exists() || !srcFile.isFile()) {
				throw new IllegalStateException("srcFile " + srcFile + " does not exist");
			}

			CompressionType srcCompressionType = getSrcCompressionType(srcFile);

			if (modes.contains(Mode.ALL) || modes.contains(Mode.ANDROID)) {
				THREAD_POOL.execute(new ConverterWorker(androidConverter, root, baseScale, srcFile, ConverterUtil.getWithoutExtension(srcFile),useSameCompressionAsSrc ? srcCompressionType : compressionType, compressionQuality));
			}
			if (modes.contains(Mode.ALL) || modes.contains(Mode.IOS)) {
				THREAD_POOL.execute(new ConverterWorker(iosConverter, root, baseScale, srcFile, ConverterUtil.getWithoutExtension(srcFile),useSameCompressionAsSrc ? srcCompressionType : compressionType, compressionQuality));
			}
		}

		THREAD_POOL.shutdown();
		try {
			THREAD_POOL.awaitTermination(180,TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("execution finished ("+(System.currentTimeMillis()-begin)+"ms)");
	}

	private static CompressionType getSrcCompressionType(File srcFile) {
		String extension = ConverterUtil.getFileExtension(srcFile);
		switch (extension) {
			case "jpg":
			case "jpeg":
				return CompressionType.JPEG;
			case "png":
				return CompressionType.PNG;
			case "gif":
				return CompressionType.GIF;
			default:
				throw new IllegalArgumentException("unknown file extension "+extension+" in srcFile "+srcFile);
		}
	}

	private static class ConverterWorker implements Runnable {
		private IPlatformConverter converter;
		private File dstFolder;
		private double baseScale;
		private File srcFile;
		private String targetFileName;
		private CompressionType compressionType;
		private float compressionQuality;

		public ConverterWorker(IPlatformConverter converter, File dstFolder, double baseScale, File srcFile, String targetFileName, CompressionType compressionType, float compressionQuality) {
			this.converter = converter;
			this.dstFolder = dstFolder;
			this.baseScale = baseScale;
			this.srcFile = srcFile;
			this.targetFileName = targetFileName;
			this.compressionType = compressionType;
			this.compressionQuality = compressionQuality;
		}

		@Override
		public void run() {
			try {
				BufferedImage srcImage = ConverterUtil.loadImage(srcFile.getAbsolutePath());
				converter.convert(dstFolder, srcImage, baseScale, targetFileName,compressionType,compressionQuality);
			} catch (Exception e) {
				System.out.println("Could not load or convert " + targetFileName + ": " + e.getMessage());
			}
		}
	}
}
