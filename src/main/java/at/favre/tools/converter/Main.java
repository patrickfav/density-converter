package at.favre.tools.converter;

import at.favre.tools.converter.arg.Arguments;
import at.favre.tools.converter.ui.CLInterpreter;
import at.favre.tools.converter.ui.GUI;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * Entry point of the app
 */
public class Main {
	public static void main(String[] rawArgs) {

		if (rawArgs.length < 1) {
			new GUI().launchApp(rawArgs);
			return;
		}

		Arguments args = CLInterpreter.parse(rawArgs);

		if (args == null) {
			return;
		} else if (args == Arguments.START_GUI) {
			System.out.println("start gui");
			new GUI().launchApp(rawArgs);
			return;
		}

		new ConverterHandler().execute(args, new ConverterHandler.HandlerCallback() {
			private NumberFormat nf = NumberFormat.getInstance(Locale.getDefault());

			@Override
			public void onProgress(float progress, String log) {
				nf.setMaximumFractionDigits(2);
				nf.setRoundingMode(RoundingMode.HALF_UP);
				System.out.print("--> " + nf.format(progress * 100f) + "% ");
			}

			@Override
			public void onFinished(int finishedJobs, List<Exception> exceptions, long time, boolean haltedDuringProcess, String log) {
				if (args.verboseLog) {
					System.out.println("Log:");
					System.out.println(log);
				}

				if (haltedDuringProcess) {
					System.err.println("abort due to error");
				}
				if (exceptions.size() > 0) {
					System.err.println("found " + exceptions.size() + " errors during execution");
					if (args.verboseLog) {
						for (Exception exception : exceptions) {
							System.err.println("\terror: " + exception.getMessage());
							exception.printStackTrace();
						}
					}
				}
				System.out.println("execution finished (" + time + "ms) with " + finishedJobs + " finsihed jobs and " + exceptions.size() + " errors");
			}
		}, true);
	}
}
