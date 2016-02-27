package at.favre.tools.converter;

/**
 * Entrypoint of the app
 */
public class Main {
	public static void main(String[] rawArgs) {
		Arguments args = CLInterpreter.parse(rawArgs);

		if (args == null) {
			return;
		} else if (args == Arguments.START_GUI) {
			System.out.println("start gui");
			//TODO start ui
			return;
		}

		if (args.verboseLog) {
			System.out.println("\nArguments: " + args + "\n");
		}

		new ConverterHandler().execute(args);
	}
}
